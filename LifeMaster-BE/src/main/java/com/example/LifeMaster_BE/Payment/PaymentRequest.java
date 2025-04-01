package com.example.LifeMaster_BE.Payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private String orderId;
    private String productName;
    private int amount;
    // Getters and setters
}
