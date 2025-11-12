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

        String token = tokenService.createToken(memberEntity.getId());
        String resetLink = "https://api.lifemaster.harvester.kr/auth/password/reset/verify?token=" + token;
//        String resetLink = "http://localhost:8080/auth/password/reset/verify?token=" + token;
        // HTML 형식으로 작성
        String htmlContent = String.format(
                "<html>" +
                        "<body>" +
                        "<h2>비밀번호 재설정</h2>" +
                        "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요:</p>" +
                        "<a href='%s' style='display:inline-block; padding:10px 20px; background-color:#4CAF50; color:white; text-decoration:none; border-radius:5px;'>비밀번호 재설정</a>" +
                        "<p>링크가 작동하지 않으면 다음 URL을 복사하여 브라우저에 붙여넣으세요:</p>" +
                        "<p>%s</p>" +
                        "</body>" +
                        "</html>",
                resetLink, resetLink
        );

        emailService.sendEmail(email, "비밀번호 재설정", htmlContent);
    }

    public void verifyToken(String token){
        tokenService.validateToken(token);
    }

    public void resetPassword(String token, String newPassword, String checkPassword){

        if(!confirmPassword(newPassword, checkPassword)){
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

    private boolean confirmPassword(String password, String confirmPassword){
        return password.equals(confirmPassword);
    }

}
