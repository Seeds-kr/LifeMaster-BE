package com.example.LifeMaster_BE.UserManager.Email.Register;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterService {

    private final RegisterRepository registerRepository;

    public void registerMember(String email, String password, String passwordConfirm) {

        if(!confirmPassword(password, passwordConfirm)){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        MemberEntity memberEntity = new MemberEntity(email, password);
        registerRepository.save(memberEntity);
    }

    public void registerMemberWithNickname(String nickname, MultipartFile image){
        if(checkNicknameDuplicate(nickname)){
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        MemberEntity memberEntity = registerRepository.findByNickname(nickname)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        memberEntity.setNickname(nickname);
        // 프로필 사진 설정 기능
    }

    private boolean confirmPassword(String password, String confirmPassword){
        return password.equals(confirmPassword);
    }

    private boolean checkNicknameDuplicate(String nickname){
        return registerRepository.existsByNickname(nickname);
    }
}
