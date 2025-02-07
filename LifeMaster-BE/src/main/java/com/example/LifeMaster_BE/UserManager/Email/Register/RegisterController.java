package com.example.LifeMaster_BE.UserManager.Email.Register;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Peristalsis.OAuthUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/user/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;
    private final OAuthUsersRepository OAuthUsersRepository;

    @PostMapping
    public ResponseEntity<Object> register(@RequestBody RegisterDto registerDto) {
        String password = registerDto.getPassword();
        String passwordConfirm = registerDto.getPasswordConfirm();
        String email = registerDto.getEmail();

        // 연동 로그인 이메일 사용 방지
        if (OAuthUsersRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 해당 계정은 연동 계정입니다. 연동 로그인을 이용하세요.");
        }

        MemberEntity memberEntity = registerService.registerMember(email, password, passwordConfirm);
        return ResponseEntity.ok(memberEntity);
    }

    @PostMapping("/nickname")
    public ResponseEntity<String> registerNickname(@ModelAttribute RegisterWithNicknameDto registerWithNicknameDto) {
        Long id = registerWithNicknameDto.getId();
        String nickName = registerWithNicknameDto.getNickName();
        MultipartFile image = registerWithNicknameDto.getImage();

        System.out.println("nickName:" + nickName);

        registerService.registerMemberWithNickname(id, nickName, image);
        return ResponseEntity.ok("saved successfully");
    }
}
