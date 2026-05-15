package com.example.LifeMaster_BE.Admin.Premium;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NonPremiumMemberAdminDto {

    private Long memberId;
    private String email;
    private String name;

    public static NonPremiumMemberAdminDto from(MemberEntity member) {
        return NonPremiumMemberAdminDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }
}