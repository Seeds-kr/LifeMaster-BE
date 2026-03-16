package com.example.LifeMaster_BE.UserManagerTest;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2AccessToken;
import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2UserService;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleOAuth2AuthenticationResponse;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleOAuthProperties;
import com.example.LifeMaster_BE.UserManager.Peristalsis.OAuthUsersEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class GoogleOAuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CustomOAuth2UserService customOAuth2UserService;
    @MockBean private GoogleOAuthProperties googleOAuthProperties;
    @MockBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("Google 공개 페이지 테스트")
    void testPublicPage() throws Exception {
        mockMvc.perform(get("/googleLogin/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is public Page"));
    }

    @Test
    @DisplayName("Google 인증 URL 생성 테스트")
    void testAuthUrlGeneration() throws Exception {
        when(googleOAuthProperties.getClientId()).thenReturn("test-client-id");
        when(googleOAuthProperties.getRedirectUri()).thenReturn("http://localhost/google/callback");

        mockMvc.perform(get("/googleLogin/authUrl"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("https://accounts.google.com/o/oauth2/v2/auth")));
    }

    @Test
    @DisplayName("Google OAuth2 콜백 테스트 (Mock)")
    void testOAuth2Callback() throws Exception {
        // 유저 객체 구성
        OAuthUsersEntity user = new OAuthUsersEntity();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setName("GoogleUser");
        user.setPicture("http://example.com/profile.jpg");

        // 토큰 구성
        Instant now = Instant.now();
        CustomOAuth2AccessToken token = new CustomOAuth2AccessToken(
                org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                now,
                now.plusSeconds(3600),
                Set.of("refresh-token")
        );

        // 응답 구성
        GoogleOAuth2AuthenticationResponse response = new GoogleOAuth2AuthenticationResponse(user, token, "mock-jwt-token");

        when(customOAuth2UserService.handleOAuth2AuthenticationGoogle(any())).thenReturn(response);

        mockMvc.perform(get("/googleLogin/callback").param("code", "auth-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.user.name").value("GoogleUser"))
                .andExpect(jsonPath("$.token.tokenValue").value("access-token"))
                .andExpect(jsonPath("$.token.refreshTokens[0]").value("refresh-token"))
                .andExpect(jsonPath("$.jwtToken").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("Google 리프레시 토큰으로 액세스 토큰 갱신 테스트 (Mock)")
    void testRefreshToken() throws Exception {
        Instant now = Instant.now();
        CustomOAuth2AccessToken newToken = new CustomOAuth2AccessToken(
                org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
                "new-access-token",
                now,
                now.plusSeconds(3600),
                Set.of("new-refresh-token")
        );

        when(customOAuth2UserService.refreshAccessTokenGoogle(any())).thenReturn(newToken);

        mockMvc.perform(post("/googleLogin/refreshToken").param("refreshToken", "old-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.tokenValue").value("new-access-token"))
                .andExpect(jsonPath("$.token.refreshTokens[0]").value("new-refresh-token"));
    }
}

