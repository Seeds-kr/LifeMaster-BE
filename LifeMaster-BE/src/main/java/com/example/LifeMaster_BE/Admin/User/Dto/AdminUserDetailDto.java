package com.example.LifeMaster_BE.Admin.User.Dto;

import com.example.LifeMaster_BE.UserManager.Member.*;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentStatus;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailDto {

    private Long id;
    private String email;
    private String nickname;
    private String imageUrl;
    private LoginType loginType;
    private LoginRole loginRole;
    private MemberStatus memberStatus;
    private int warningCount;
    private SubscriptionPlan subscriptionPlan;
    private PaymentStatus paymentStatus;
    private LocalDate lastPaymentDate;
    private LocalDate expirationDate;
    private LocalDateTime createdAt;
    private int groupCount;
    private int reportCount;

    public static AdminUserDetailDto from(MemberEntity member) {
        return AdminUserDetailDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .imageUrl(member.getImageUrl())
                .loginType(member.getLoginType())
                .loginRole(member.getLoginRole())
                .memberStatus(member.getMemberStatus())
                .warningCount(member.getWarningCount())
                .subscriptionPlan(member.getSubscriptionPlan())
                .paymentStatus(member.getPaymentStatus())
                .lastPaymentDate(member.getLastPaymentDate())
                .expirationDate(member.getExpirationDate())
                .createdAt(member.getCreatedAt())
                .groupCount(member.getGroups() != null ? member.getGroups().size() : 0)
                .reportCount(member.getReports() != null ? member.getReports().size() : 0)
                .build();
    }
}
