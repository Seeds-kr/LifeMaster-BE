package com.example.LifeMaster_BE.UserManager.Email.Register.Dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

    private String email;
    private String password;
    private String passwordConfirm;
}