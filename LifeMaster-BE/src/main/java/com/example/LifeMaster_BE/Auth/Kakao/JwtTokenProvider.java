package com.example.LifeMaster_BE.Auth.Kakao;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final long tokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.kakao.secretKey}") String secretKey,
            @Value("${jwt.kakao.tokenValidityInMilliseconds}") long tokenValidityInMilliseconds,
            @Value("${jwt.kakao.refreshTokenValidityInMilliseconds}") long refreshTokenValidityInMilliseconds) {
        this.secretKey = secretKey;
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }
    // 액세스 토큰 생성
    public String createAccessToken(Long userId) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId)); // subject에 userId 설정
        claims.put("role", "USER"); // 추가적인 클레임 설정 가능

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds); // 토큰 만료 시간

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명
                .compact();
    }

    // 리프레시 토큰 생성 (랜덤 값으로)
    public String createRefreshToken() {
        return UUID.randomUUID().toString(); // 랜덤 값으로 리프레시 토큰 생성
    }

    // JWT에서 userId 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.valueOf(claims.getSubject()); // subject에 저장된 userId 반환
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
