package com.example.LifeMaster_BE.UserManager.Email.Password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("createToken() - 정상 토큰 생성")
    void createToken_shouldStoreTokenInRedis(){
        Long userId = 1L;

        String token = tokenService.createToken(userId);

        assertNotNull(token);
        verify(valueOperations).set(eq(token), eq(String.valueOf(userId)), any());
    }

    @Test
    @DisplayName("validateToken() - 유효한 토큰")
    void validateToken_valid(){
        String token = "valid-token";

        when(redisTemplate.hasKey(token)).thenReturn(true);

        assertDoesNotThrow(() -> tokenService.validateToken(token));
    }

    @Test
    @DisplayName("validateToken() - 유효하지 않은 토큰")
    void validateToken_inValid(){
        String token = "invalid-token";

        when(redisTemplate.hasKey(token)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> tokenService.validateToken(token));
    }

    @Test
    @DisplayName("validateAndConsumeToken() - 정상 동작")
    void validateAndConsumeToken_valid(){
        String token = "token123";
        String userId = "456";

        when(valueOperations.get(token)).thenReturn(userId);
        Long result = tokenService.validateAndConsumeToken(token);

        assertEquals(456L, result);
        verify(redisTemplate).delete(token);
    }

    @Test
    @DisplayName("validateAndConsumeToken() - 잘못된 토큰")
    void validateAndConsumeToken_invalid(){
        String token = "invalid-token";

        when(valueOperations.get(token)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> tokenService.validateAndConsumeToken(token));
    }

}