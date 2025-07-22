package com.example.LifeMaster_BE.UserManagerTest;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2AccessToken;
import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2UserService;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleOAuth2AuthenticationResponse;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Naver.NaverOAuthProperties;
import com.example.LifeMaster_BE.UserManager.Peristalsis.OAuthUsersEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class NaverOAuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CustomOAuth2UserService customOAuth2UserService;
    @MockBean private NaverOAuthProperties naverOAuthProperties;
    @MockBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("공개 페이지 접근 테스트")
    void testPublicPage() throws Exception {
        mockMvc.perform(get("/naverLogin/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is public Page"));
    }

    @Test
    @DisplayName("인증 URL 생성 테스트")
    void testAuthUrlGeneration() throws Exception {
        when(naverOAuthProperties.getClientId()).thenReturn("test-client-id");
        when(naverOAuthProperties.getRedirectUri()).thenReturn("http://localhost/callback");
        when(naverOAuthProperties.getScope()).thenReturn("email");

        mockMvc.perform(get("/naverLogin/authUrl"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://nid.naver.com/oauth2.0/authorize")));
    }

    @Test
    @DisplayName("네이버 OAuth2 콜백 테스트 (Mock)")
    void testOAuth2Callback() throws Exception {
        OAuthUsersEntity user = new OAuthUsersEntity();
        user.setId(1L);
        user.setEmail("test@naver.com");
        user.setName("테스트유저");
        user.setPicture("http://test.com/pic.jpg");

        Instant now = Instant.now();
        CustomOAuth2AccessToken token = new CustomOAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                now,
                now.plusSeconds(3600),
                Set.of("refresh-token")
        );

        GoogleOAuth2AuthenticationResponse response = new GoogleOAuth2AuthenticationResponse(user, token, "mock-jwt");

        when(customOAuth2UserService.handleOAuth2AuthenticationNaver(any()))
                .thenReturn(response);

        mockMvc.perform(get("/naverLogin/callback").param("code", "auth-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@naver.com"))
                .andExpect(jsonPath("$.token.tokenValue").value("access-token"))
                .andExpect(jsonPath("$.jwtToken").value("mock-jwt"));
    }


    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰 갱신 테스트 (Mock)")
    void testRefreshToken() throws Exception {
        Instant now = Instant.now();
        CustomOAuth2AccessToken newToken = new CustomOAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "new-access-token",
                now,
                now.plusSeconds(3600),
                Set.of("new-refresh-token")
        );

        when(customOAuth2UserService.refreshAccessTokenNaver(any()))
                .thenReturn(newToken);

        mockMvc.perform(post("/naverLogin/refreshToken")
                        .param("refreshToken", "old-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.tokenValue").value("new-access-token"))
                .andExpect(jsonPath("$.token.refreshTokens[0]").value("new-refresh-token"));
    }
}

