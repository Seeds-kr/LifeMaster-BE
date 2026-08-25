package com.example.LifeMaster_BE.UserManager.Email.Register;

import com.example.LifeMaster_BE.Exception.CustomException.ConflictException;
import com.example.LifeMaster_BE.UserManager.Email.Register.Dto.RegistrationCacheDto;
import com.example.LifeMaster_BE.UserManager.Email.Register.Dto.Response.RegistrationInitResponseDto;
import com.example.LifeMaster_BE.UserManager.Email.Register.Dto.Request.RegisterDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.LifeMaster_BE.UserManager.Member.LoginType;
import com.example.LifeMaster_BE.UserManager.Member.MemberStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterService {

    private final S3Service s3Service;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public RegistrationInitResponseDto registerMember(RegisterDto registerDto) {
        String email = registerDto.getEmail();
        String password = registerDto.getPassword();
        String passwordConfirm = registerDto.getPasswordConfirm();

        checkBeforeRegister(email, password, passwordConfirm);

        String encodedPassword = encodePassword(password);
        String regId = UUID.randomUUID().toString();
        RegistrationCacheDto redisRegisterDto = new RegistrationCacheDto(email, encodedPassword);

        redisTemplate.opsForValue().set("reg:" + regId, redisRegisterDto, Duration.ofMinutes(5));
        log.info(regId);
        return new RegistrationInitResponseDto(regId);
    }

    public Long registerMemberWithNickname(String regId, String nickname, MultipartFile image){
        String key = "reg:" + regId;
        log.info(regId);
        RegistrationCacheDto cachedData = (RegistrationCacheDto) redisTemplate.opsForValue().get(key);
        if(cachedData == null){
            throw new IllegalStateException("회원가입 세션이 만료되었거나 잘못된 key 입니다.");
        }

        String email = cachedData.getEmail();
        String encodedPassword = cachedData.getEncodedPassword();
        MemberEntity newMember = new MemberEntity(email, encodedPassword, nickname);
        newMember.setLoginType(LoginType.EMAIL);

        // 프로필 사진 설정
        if(image != null){
            try{
                String fileUrl = s3Service.uploadFile(image);
                newMember.setImageUrl(fileUrl);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e); // 런타임 예외로 변환
            }
        }

        try{
            memberRepository.save(newMember);
        }catch (DataIntegrityViolationException e){
            throw new ConflictException("UNIQUE_VIOLATION", "이메일 또는 닉네임이 이미 사용 중입니다.");
        }

        return newMember.getId();
    }

    public boolean checkNicknameAvailable(String nickname){
        // 탈퇴하지 않은 회원 중에서 닉네임 중복 체크
        return memberRepository.existsByNicknameAndMemberStatusNot(nickname, MemberStatus.DELETED);
    }

    private void checkBeforeRegister(String email, String password, String confirmPassword){

        if(checkEmailDuplicate(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        if(!confirmPassword(password, confirmPassword)){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
    }

    private boolean checkEmailDuplicate(String email){
        // 탈퇴하지 않은 회원 중에서 이메일 중복 체크
        return memberRepository.existsByEmailAndMemberStatusNot(email, MemberStatus.DELETED);
    }

    private boolean confirmPassword(String password, String confirmPassword){
        return password.equals(confirmPassword);
    }

    private String encodePassword(String password){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.encode(password);
    }
}
