package com.example.LifeMaster_BE.Security.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final SecretKey SECRETE_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long EXPIRATION_TIME;

    public JwtUtil() {
        this(1000 * 60 * 60 * 24); // 기본 24시간
    }

    // 테스트 편의를 위한 생성자
    public JwtUtil(long expirationTime) {
        EXPIRATION_TIME = expirationTime;
    }

    // 🔑 SecretKey 리턴 메서드 추가
    public SecretKey getSecretKey() {
        return SECRETE_KEY;
    }

    // jwt 생성
    public String generateToken(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRETE_KEY)
                .compact();
    }

    public String generateRefreshToken(String email) {
        // refresh token은 만료시간 더 길게 (예: 2주)
        long refreshExpiration = 1000L * 60 * 60 * 24 * 14;
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSecretKey())
                .compact();
    }

    // JWT에서 사용자 email 추출.
    public String extractEmail(String token){
        return Jwts.parserBuilder()
                .setSigningKey(SECRETE_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 외부에서 호출할 토큰 검증 매서드
    public boolean isTokenValid(String token){
        return isTokenSignatureValid(token) && isTokenNotExpired(token);
    }

    // JWT 서명 및 무결성 검사
    private boolean isTokenSignatureValid(String token){
        try{
            Jwts.parserBuilder().setSigningKey(SECRETE_KEY).build().parseClaimsJws(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    // JWT 만료 여부 검증
    private boolean isTokenNotExpired(String token){
        return !extractClaims(token).getExpiration().before(new Date());
    }

    // JWT payload 반환
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRETE_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
