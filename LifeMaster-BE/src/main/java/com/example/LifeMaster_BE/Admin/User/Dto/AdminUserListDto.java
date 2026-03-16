package com.example.LifeMaster_BE.Admin.User.Dto;

import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.LoginType;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListDto {

    private Long id;
    private String email;
    private String nickname;
    private LoginType loginType;
    private LoginRole loginRole;
    private MemberStatus memberStatus;
    private int warningCount;
    private LocalDateTime createdAt;

    public static AdminUserListDto from(MemberEntity member) {
        return AdminUserListDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .loginType(member.getLoginType())
                .loginRole(member.getLoginRole())
                .memberStatus(member.getMemberStatus())
                .warningCount(member.getWarningCount())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
