package com.example.LifeMaster_BE.UserManager.Email.Login;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginDto {
    private String email;
    private String password;
}
