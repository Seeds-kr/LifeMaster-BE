package com.example.LifeMaster_BE.Payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiptRequest {
    private String subscriptionId;
    private String purchaseToken;
}
