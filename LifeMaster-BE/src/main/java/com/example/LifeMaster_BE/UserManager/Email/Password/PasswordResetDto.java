package com.example.LifeMaster_BE.UserManager.Email.Password;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {

    private String token;

    private String newPassword;
    private String checkPassword;
}
