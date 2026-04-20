package com.example.LifeMaster_BE.Admin.Premium;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class PremiumMemberAdminDto {
    private Long memberId;
    private String email;
    private String name;
    private LocalDate subscriptionExpirationDate;
    private boolean expired;

    public static PremiumMemberAdminDto from(MemberEntity member) {
        LocalDate expirationDate = member.getSubscriptionExpirationDate();
        boolean expired = expirationDate == null || expirationDate.isBefore(LocalDate.now());

        return PremiumMemberAdminDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .subscriptionExpirationDate(expirationDate)
                .expired(expired)
                .build();
    }
}
