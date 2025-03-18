package com.example.LifeMaster_BE.GooglePlay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiptRequest {
    private String subscriptionId;
    private String purchaseToken;
}
