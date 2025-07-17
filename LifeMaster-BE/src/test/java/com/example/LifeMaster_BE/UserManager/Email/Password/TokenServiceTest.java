package com.example.LifeMaster_BE.UserManager.Email.Password;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @Test
    @DisplayName("토큰 생성 및 검증 성공")
    void createAndValidateToken_success(){

        Long userId = 1L;

        String token = tokenService.createToken(userId);
        Long validateUserId = tokenService.validateAndConsumeToken(token);

        assertEquals(userId, validateUserId);
    }

    @Test
    @DisplayName("한 번 사용한 토큰은 재사용 불가")
    void tokenUsedOnce_onlyOnce(){

        String token = tokenService.createToken(1L);
        tokenService.validateToken(token); // 첫 번째 사용

        assertThrows(IllegalArgumentException.class, () ->{
            tokenService.validateToken(token); // 두 번째 사용
        });
    }

    @Test
    @DisplayName("존재하지 않는 토큰 사용 시 예외 발생")
    void invalidToken_throwsException(){
        assertThrows(IllegalArgumentException.class, () ->{
            tokenService.validateToken("non-existent-token");
        });
    }

}