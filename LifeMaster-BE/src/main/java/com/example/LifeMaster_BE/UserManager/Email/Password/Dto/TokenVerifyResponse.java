package com.example.LifeMaster_BE.UserManager.Email.Password.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenVerifyResponse {

    private boolean success;
    private String message;
    private String token;
}
