package com.example.LifeMaster_BE.UserManager.Email.Register;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterService {

    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    public MemberEntity registerMember(String email, String password, String passwordConfirm) {

        if(!confirmPassword(password, passwordConfirm)){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        String encodedPassword = encodePassword(password);
        MemberEntity memberEntity = new MemberEntity(email, encodedPassword);
        return memberRepository.save(memberEntity);
    }

    public void registerMemberWithNickname(String nickname, MultipartFile image){
        if(checkNicknameDuplicate(nickname)){
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        MemberEntity memberEntity = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        memberEntity.setNickname(nickname);
        // 프로필 사진 설정
        if(!image.isEmpty()){
            try{
                String fileUrl = s3Service.uploadFile(image);
                memberEntity.setImageUrl(fileUrl);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e); // 런타임 예외로 변환
            }
        }
    }

    private boolean confirmPassword(String password, String confirmPassword){
        return password.equals(confirmPassword);
    }

    private boolean checkNicknameDuplicate(String nickname){
        return memberRepository.existsByNickname(nickname);
    }

    private String encodePassword(String password){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.encode(password);
    }
}
