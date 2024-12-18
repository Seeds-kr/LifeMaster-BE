package com.example.LifeMaster_BE.UserManager.Email.Register;

import lombok.Getter;

@Getter
public class RegisterDto {
    private String email;
    private String password;
    private String passwordConfirm;
}
