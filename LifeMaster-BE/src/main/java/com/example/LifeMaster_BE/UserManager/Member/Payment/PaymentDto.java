package com.example.LifeMaster_BE.UserManager.Member.Payment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentDto {
    private Long id;
    private double amount;
    private LocalDate paymentDate;
    private String paymentStatus;  // 예: "PAID", "PENDING", "FAILED"

    public static PaymentDto from(PaymentEntity payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setPaymentStatus(payment.getPaymentStatus().name()); // Enum -> String 변환
        return dto;
    }
}

