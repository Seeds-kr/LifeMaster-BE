package com.example.LifeMaster_BE.UserManager.Email.Register.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckNicknameResponseDto {
    boolean available;
}
