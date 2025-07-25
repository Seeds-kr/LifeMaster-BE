package com.example.LifeMaster_BE.UserManager.Email.Password;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(10);

    public String createToken(Long userId){
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(token, String.valueOf(userId), TOKEN_EXPIRATION);
        return token;
    }

    public void validateToken(String token){
        Boolean hasKey = redisTemplate.hasKey(token);
        if (hasKey == null || !hasKey){
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public Long validateAndConsumeToken(String token){
        String userIdStr = redisTemplate.opsForValue().get(token);
        if(userIdStr == null){
            throw new IllegalArgumentException("Invalid token");
        }
        redisTemplate.delete(token);
        return Long.valueOf(userIdStr);
    }
}
