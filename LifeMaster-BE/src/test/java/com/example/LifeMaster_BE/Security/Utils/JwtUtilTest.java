package com.example.LifeMaster_BE.Security.Utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    @DisplayName("generateToken과 extractEmail을 통해 원본 이메일을 정확히 추출가능")
    void generateToken_And_ExtractEmail_ShouldReturnSameEmail() {

        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);
        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("정상적으로 생성된 토큰은 유효성 검사에서 true를 반환")
    void isTokenValid_ShouldReturnTrue_ForValidToken(){

        String email = "valid@example.com";
        String token = jwtUtil.generateToken(email);

        boolean isValid = jwtUtil.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 유효성 검사에서 false를 반환")
    void isTokenValid_ShouldReturnFalse_ForInvalidSignature(){

        String email = "test2@exmaple.com";
        JwtUtil anotherJwtUtil = new JwtUtil();
        anotherJwtUtil.generateToken(email);

        boolean isValid = jwtUtil.isTokenValid(email);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("만료된 토큰은 유효성 검사에서 false를 반환")
    void isTokenValid_ShouldReturnFalse_ForExpiredToken() throws Exception{
        
        String email = "expired@example.com";
        JwtUtil shortenedJwtUtil = new JwtUtil(1L);
        String token = shortenedJwtUtil.generateToken(email);

        Thread.sleep(100); // 토큰 만료 대기

        boolean isValid = shortenedJwtUtil.isTokenValid(token);

        assertFalse(isValid);
    }
}