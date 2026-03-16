package com.example.LifeMaster_BE.UserManager.Email.Password.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyCodeRequest {
    private String email;
    private String code;
}
