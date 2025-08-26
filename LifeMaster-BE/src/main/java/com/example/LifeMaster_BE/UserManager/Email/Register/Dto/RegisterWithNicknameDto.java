package com.example.LifeMaster_BE.UserManager.Email.Register.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class RegisterWithNicknameDto {

    @Schema(description = "회원가입 1단계 반환값", example = "6a5e3b41-...")
    private String regId;

    @Schema(description = "닉네임", example = "randomNickName")
    private String nickName;

    @Schema(type = "string", format = "binary", description = "프로필 이미지")
    private MultipartFile image;


//    @Schema(description = "회원가입 ID", example = "reg123")
//    private String regId;
//
//    @Schema(description = "닉네임", example = "gomz")
//    private String nickName;
//
//    @Schema(type = "string", format = "binary", description = "프로필 이미지")
//    private MultipartFile image;
}
