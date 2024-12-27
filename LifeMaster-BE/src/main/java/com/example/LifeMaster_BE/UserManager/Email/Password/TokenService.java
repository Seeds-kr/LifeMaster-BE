package com.example.LifeMaster_BE.UserManager.Email.Password;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Map<String, Long> tokenStorage = new ConcurrentHashMap<>();

    public String createToken(Long userId){
        String token = UUID.randomUUID().toString();
        tokenStorage.put(token, userId);
        return token;
    }

    public Long validateToken(String token){
        if(!tokenStorage.containsKey(token)){
            throw new IllegalArgumentException("Invalid token");
        }

        Long userId = tokenStorage.get(token);
        tokenStorage.remove(token);
        return userId;
    }
}
