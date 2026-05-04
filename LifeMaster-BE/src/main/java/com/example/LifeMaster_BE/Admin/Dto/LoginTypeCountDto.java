package com.example.LifeMaster_BE.Admin.Dto;

import com.example.LifeMaster_BE.UserManager.Member.LoginType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginTypeCountDto {
    private LoginType loginType;
    private Long count;
}
