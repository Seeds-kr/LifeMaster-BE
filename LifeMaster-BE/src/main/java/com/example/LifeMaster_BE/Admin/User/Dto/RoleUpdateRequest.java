package com.example.LifeMaster_BE.Admin.User.Dto;

import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoleUpdateRequest {

    @NotNull(message = "역할은 필수입니다.")
    private LoginRole loginRole;
}
