package com.example.LifeMaster_BE.UserManager.Email.Password;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PasswordService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final TokenService tokenService;

    public void sendPasswordResetEmail(String email) {
        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일 정보입니다."));

        String code = tokenService.createVerificationCode(email, memberEntity.getId());

        String htmlContent = String.format(
                "<html>" +
                        "<body>" +
                        "<h2>비밀번호 재설정</h2>" +
                        "<p>아래 인증 코드를 입력하여 비밀번호를 재설정하세요:</p>" +
                        "<div style='background-color:#f4f4f4; padding:20px; text-align:center; margin:20px 0;'>" +
                        "<span style='font-size:32px; font-weight:bold; letter-spacing:8px; color:#333;'>%s</span>" +
                        "</div>" +
                        "<p>이 코드는 10분 동안 유효합니다.</p>" +
                        "<p>본인이 요청하지 않았다면 이 이메일을 무시해주세요.</p>" +
                        "</body>" +
                        "</html>",
                code
        );

        emailService.sendEmail(email, "비밀번호 재설정 인증 코드", htmlContent);
    }

    public String verifyCode(String email, String code) {
        return tokenService.verifyCodeAndCreateToken(email, code);
    }

    public void resetPassword(String token, String newPassword, String checkPassword) {
        if (!confirmPassword(newPassword, checkPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        Long userId = tokenService.validateAndConsumeToken(token);
        MemberEntity memberEntity = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid member"));

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = bCryptPasswordEncoder.encode(newPassword);

        memberEntity.setPassword(hashedPassword);
        memberRepository.save(memberEntity);
    }

    private boolean confirmPassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }
}
