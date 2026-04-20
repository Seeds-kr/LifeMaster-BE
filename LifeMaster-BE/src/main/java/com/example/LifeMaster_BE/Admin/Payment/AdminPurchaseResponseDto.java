package com.example.LifeMaster_BE.Admin.Payment;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminPurchaseResponseDto {

    private String purchaseToken;
    private Long memberId;
    private String memberEmail;
    private String memberNickname;

    private String provider;
    private String productName;
    private Integer amount;
    private String currency;

    private String orderId;
    private String purchaseType;
    private String purchaseState;
    private LocalDateTime purchaseTime;

    public static AdminPurchaseResponseDto from(PurchaseEntity entity) {
        return AdminPurchaseResponseDto.builder()
                .purchaseToken(entity.getPurchaseToken())
                .memberId(entity.getMember() != null ? entity.getMember().getId() : null)
                .memberEmail(entity.getMember() != null ? entity.getMember().getEmail() : null)
                .memberNickname(entity.getMember() != null ? entity.getMember().getNickname() : null)
                .provider(entity.getProvider())
                .productName(entity.getProductName())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .orderId(entity.getOrderId())
                .purchaseType(entity.getPurchaseType())
                .purchaseState(entity.getPurchaseState())
                .purchaseTime(entity.getPurchaseTime())
                .build();
    }
}