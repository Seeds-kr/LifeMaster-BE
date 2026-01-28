package com.example.LifeMaster_BE.UserManager.Email.Password;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS = 5;
    private static final String CODE_KEY_PREFIX = "password_reset:code:";
    private static final String ATTEMPTS_KEY_PREFIX = "password_reset:attempts:";
    private static final String USERID_KEY_PREFIX = "password_reset:userid:";

    // 6자리 인증 코드 생성 및 저장
    public String createVerificationCode(String email, Long userId) {
        String code = generateSixDigitCode();
        String codeKey = CODE_KEY_PREFIX + email;
        String attemptsKey = ATTEMPTS_KEY_PREFIX + email;
        String userIdKey = USERID_KEY_PREFIX + email;

        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRATION);
        redisTemplate.opsForValue().set(attemptsKey, "0", CODE_EXPIRATION);
        redisTemplate.opsForValue().set(userIdKey, String.valueOf(userId), CODE_EXPIRATION);

        return code;
    }

    // 6자리 숫자 코드 생성
    private String generateSixDigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // 인증 코드 검증 및 토큰 발급
    public String verifyCodeAndCreateToken(String email, String inputCode) {
        String codeKey = CODE_KEY_PREFIX + email;
        String attemptsKey = ATTEMPTS_KEY_PREFIX + email;
        String userIdKey = USERID_KEY_PREFIX + email;

        // 저장된 코드 조회
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            throw new IllegalArgumentException("인증 코드가 만료되었거나 존재하지 않습니다.");
        }

        // 시도 횟수 확인
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            deleteVerificationData(email);
            throw new IllegalArgumentException("인증 시도 횟수를 초과했습니다. 다시 인증 코드를 요청해주세요.");
        }

        // 코드 검증
        if (!storedCode.equals(inputCode)) {
            redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts + 1), CODE_EXPIRATION);
            int remainingAttempts = MAX_ATTEMPTS - attempts - 1;
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다. 남은 시도 횟수: " + remainingAttempts);
        }

        // 검증 성공 - userId 조회 후 토큰 발급
        String userIdStr = redisTemplate.opsForValue().get(userIdKey);
        if (userIdStr == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
        }

        // 인증 데이터 삭제
        deleteVerificationData(email);

        // 토큰 발급
        return createToken(Long.valueOf(userIdStr));
    }

    // 인증 데이터 삭제
    private void deleteVerificationData(String email) {
        redisTemplate.delete(CODE_KEY_PREFIX + email);
        redisTemplate.delete(ATTEMPTS_KEY_PREFIX + email);
        redisTemplate.delete(USERID_KEY_PREFIX + email);
    }

    // 토큰 생성 (비밀번호 재설정용)
    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(token, String.valueOf(userId), TOKEN_EXPIRATION);
        return token;
    }

    // 토큰 검증 및 소비
    public Long validateAndConsumeToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(token);
        if (userIdStr == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.");
        }
        redisTemplate.delete(token);
        return Long.valueOf(userIdStr);
    }
}
