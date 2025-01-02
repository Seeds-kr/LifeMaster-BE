package com.example.LifeMaster_BE.UserManager.Peristalsis;

import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.Set;

@Getter
public class CustomOAuth2AccessToken extends OAuth2AccessToken {

    private final Set<String> refreshTokens;  // 리프레시 토큰을 저장할 필드

    // 생성자에서 리프레시 토큰을 추가
    public CustomOAuth2AccessToken(TokenType tokenType, String tokenValue, Instant issuedAt, Instant expiresAt, Set<String> refreshTokens) {
        super(tokenType, tokenValue, issuedAt, expiresAt);
        this.refreshTokens = refreshTokens;
    }

    // 리프레시 토큰을 반환하는 메서드
    public Set<String> getRefreshTokens() {
        return refreshTokens;
    }
}
