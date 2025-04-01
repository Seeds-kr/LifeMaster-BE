package com.example.LifeMaster_BE.Payment;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchases_list")
@Getter
@Setter
public class PurchaseEntity {

    @Id
    private String purchaseToken;

    private String subscriptionId;

    private String packageName;

    private String purchaseState;

    private LocalDateTime purchaseTime;

    private String purchaseType;

    private String orderId;

    private String developerPayload;

}

