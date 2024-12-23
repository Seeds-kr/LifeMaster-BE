package com.example.LifeMaster_BE.UserManager.Email.Register;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterDto {
    private String email;
    private String password;
    private String passwordConfirm;
}
