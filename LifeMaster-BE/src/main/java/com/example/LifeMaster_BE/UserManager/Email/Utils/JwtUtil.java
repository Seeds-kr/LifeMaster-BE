package com.example.LifeMaster_BE.UserManager.Email.Utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey SECRETE_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간

    public String generateToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRETE_KEY)
                .compact();
    }

    public String extractUsername(String token){
        return Jwts.parserBuilder()
                .setSigningKey(SECRETE_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(SECRETE_KEY).build().parseClaimsJws(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }
}
