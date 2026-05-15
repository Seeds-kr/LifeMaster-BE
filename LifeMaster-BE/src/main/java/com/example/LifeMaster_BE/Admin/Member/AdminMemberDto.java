package com.example.LifeMaster_BE.Admin.Member;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentStatus;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionPlan;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberDto {

    private Long memberId;
    private String email;
    private String nickname;

    private String loginType;
    private boolean loginStatus;

    private String loginRole;
    private String memberStatus;

    private int warningCount;

    private SubscriptionPlan subscriptionPlan;
    private PaymentStatus paymentStatus;
    private LocalDate expirationDate;

    private LocalDateTime createdAt;

    public static AdminMemberDto from(MemberEntity member) {
        return AdminMemberDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .loginType(member.getLoginType() != null ? member.getLoginType().name() : "-")
                .loginStatus(member.isLoginStatus())
                .loginRole(member.getLoginRole() != null ? member.getLoginRole().name() : "-")
                .memberStatus(member.getMemberStatus() != null ? member.getMemberStatus().name() : "-")
                .warningCount(member.getWarningCount())
                .subscriptionPlan(member.getSubscriptionPlan())
                .paymentStatus(member.getPaymentStatus())
                .expirationDate(member.getExpirationDate())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
