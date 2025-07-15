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
        String resetLink = "http://lifemaster.com/auth/reset-password?token=" + token;

        emailService.sendEmail(email, "Reset Password",
                "Click the link to reset your password: " + resetLink);

    }


    public void resetPassword(String token, String newPassword, String checkPassword){

        if(!confirmPassword(newPassword, checkPassword)){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        Long userId = tokenService.validateToken(token);

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
