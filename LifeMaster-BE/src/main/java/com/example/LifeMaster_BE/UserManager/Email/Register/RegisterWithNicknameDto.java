package com.example.LifeMaster_BE.UserManager.Email.Register;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class RegisterWithNicknameDto {

    private Long id;
    private String nickName;
    private MultipartFile image;
}
