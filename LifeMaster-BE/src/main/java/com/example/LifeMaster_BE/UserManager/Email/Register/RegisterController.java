package com.example.LifeMaster_BE.UserManager.Email.Register;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
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

    @PostMapping
    public ResponseEntity<MemberEntity> register(@RequestBody RegisterDto registerDto){
        String password = registerDto.getPassword();
        String passwordConfirm = registerDto.getPasswordConfirm();
        String email = registerDto.getEmail();

        MemberEntity memberEntity = registerService.registerMember(email, password, passwordConfirm);
        return ResponseEntity.ok(memberEntity);
    }

    @PostMapping("/nickname")
    public ResponseEntity<String> registerNickname(@ModelAttribute RegisterWithNicknameDto registerWithNicknameDto) {
        String nickName = registerWithNicknameDto.getNickName();
        MultipartFile image = registerWithNicknameDto.getImage();

        registerService.registerMemberWithNickname(nickName, image);
        return ResponseEntity.ok("saved successfully");
    }
}
