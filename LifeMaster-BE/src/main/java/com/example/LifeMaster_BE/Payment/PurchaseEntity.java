package com.example.LifeMaster_BE.Payment;


import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;

    public void setId(long l) {
    }
}

