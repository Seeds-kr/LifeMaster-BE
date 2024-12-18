package com.example.LifeMaster_BE.UserManager.Email.Register;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController("/user")
@RequiredArgsConstructor
public class RegisterController {

    @PostMapping("/register")
    public String register(@RequestBody RegisterDto registerDto){
        String password = registerDto.getPassword();
        String passwordConfirm = registerDto.getPasswordConfirm();
        String email = registerDto.getEmail();

        return "ok";
    }

    @PostMapping("/register-nickname")
    public String registerNickname(@ModelAttribute RegisterWithNicknameDto registerWithNicknameDto){
        String nickName = registerWithNicknameDto.getNickName();
        MultipartFile image = registerWithNicknameDto.getImage();

        return "ok";
    }
}
