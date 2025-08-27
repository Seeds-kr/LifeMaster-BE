package com.example.LifeMaster_BE.UserManager.Email.Register.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCompleteResponseDto {

    private String message;
    private Long memberId;
}
