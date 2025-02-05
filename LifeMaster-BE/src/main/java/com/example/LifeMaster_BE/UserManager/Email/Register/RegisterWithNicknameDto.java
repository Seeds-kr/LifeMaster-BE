package com.example.LifeMaster_BE.UserManager.Email.Register;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class RegisterWithNicknameDto {

    private String nickName;
    private MultipartFile image;
}
