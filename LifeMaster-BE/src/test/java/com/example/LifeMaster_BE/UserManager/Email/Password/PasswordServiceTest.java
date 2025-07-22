package com.example.LifeMaster_BE.UserManager.Email.Password;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock EmailService emailService;

    @Mock TokenService tokenService;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 성공")
    void sendPasswordResetEmail_success(){

        String email = "test@example.com";
        Long userId = 1L;
        MemberEntity member = new MemberEntity();
        member.setId(userId);
        member.setEmail(email);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(tokenService.createToken(userId)).thenReturn("token123");

        passwordService.sendPasswordResetEmail(email);

        verify(emailService).sendEmail(eq(email), eq("Reset Password"), contains("token123"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 재설정 시도 시 예외 발생")
    void sendPasswordResetEmail_notFound_throwsException() {

        String email = "notfound@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            passwordService.sendPasswordResetEmail(email);
        });
    }

    @Test
    @DisplayName("비밀번호 재설정 - 비밀번호가 일치하지 않으면 예외 발생")
    void resetPassword_passwordMismatch_throwsException() {

        String token = "invalidToken";
        String newPassword = "password123";
        String checkPassword = "password456";

        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.resetPassword(token, newPassword, checkPassword);
        });
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPassword_success(){

        String token = "validToken";
        String newPassword = "password";
        String checkPassword = "password";
        Long userId = 1L;

        MemberEntity member = new MemberEntity();
        member.setId(userId);
        member.setEmail(token);

        when(tokenService.validateAndConsumeToken(token)).thenReturn(userId);
        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));

        passwordService.resetPassword(token, newPassword, checkPassword);

        verify(memberRepository).save(argThat(savedMember ->
                new BCryptPasswordEncoder().matches(newPassword, savedMember.getPassword())));

    }

}