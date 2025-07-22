package com.example.LifeMaster_BE.UserManager.Email.Login;

import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoService;
import com.example.LifeMaster_BE.Security.CustomUserDetailService;
import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Peristalsis.OAuthUsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private OAuthUsersRepository oAuthUsersRepository;
    @MockBean
    private TodoService todoService;
    @MockBean
    private CustomUserDetailService customUserDetailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("정상 로그인 시 토큰 반환")
    void login_success() throws Exception {

        String email = "user@example.com";
        String password = "1234";
        String token = "mock.jwt.token";

        LoginDto loginDto = new LoginDto(email, password);

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(email);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(oAuthUsersRepository.existsByEmail(email)).thenReturn(false);
        when(jwtUtil.generateToken(anyString())).thenReturn(token);


        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(token));
    }

    @Test
    @DisplayName("OAuth 계정은 로그인 불가 - 400 반환")
    void login_fail_oauth_user() throws Exception {

        String email = "oauth@example.com";
        String password = "1234";

        LoginDto loginDto = new LoginDto(email, password);

        when(oAuthUsersRepository.existsByEmail(email)).thenReturn(true);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 해당 계정은 연동 계정입니다. 연동 로그인을 이용하세요."));
    }
}