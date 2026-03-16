package com.example.LifeMaster_BE.Payment;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PurchaseDto(
        String provider,
        String orderId,
        String productName,
        Integer amount,
        String currency,
        String purchaseState,
        LocalDateTime purchaseTime,
        String purchaseToken,
        String subscriptionId,
        String packageName,
        Long memberId // 필요하면 최소한으로
) {
    public static PurchaseDto from(PurchaseEntity e) {
        return new PurchaseDto(
                e.getProvider(),
                e.getOrderId(),
                e.getProductName(),
                e.getAmount(),
                e.getCurrency(),
                e.getPurchaseState(),
                e.getPurchaseTime(),
                e.getPurchaseToken(),
                e.getSubscriptionId(),
                e.getPackageName(),
                e.getMember() != null ? e.getMember().getId() : null
        );
    }
}

