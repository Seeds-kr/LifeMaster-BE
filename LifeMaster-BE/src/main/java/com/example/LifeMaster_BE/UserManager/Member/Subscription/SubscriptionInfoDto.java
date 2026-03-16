package com.example.LifeMaster_BE.UserManager.Member.Subscription;

import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentStatus;

import java.time.LocalDate;

public record SubscriptionInfoDto(
        SubscriptionPlan subscriptionPlan,
        LocalDate lastPaymentDate,
        LocalDate expirationDate,
        PaymentStatus paymentStatus
) {}