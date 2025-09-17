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
    private String purchaseToken;   // 기본 PK

    private String subscriptionId;  // 구독 ID
    private String packageName;     // 패키지명
    private String purchaseState;   // COMPLETED / CANCELED 등
    private LocalDateTime purchaseTime;
    private String purchaseType;    // INAPP / SUBS 등
    private String orderId;
    private String developerPayload;

    // ✅ 공통 필드 추가
    @Column(length = 20, nullable = false)
    private String provider; // "PAYPAL", "NAVERPAY", "GOOGLEPLAY"

    private String productName; // 상품명
    private Integer amount;     // 결제 금액
    private String currency;    // "KRW", "USD" 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;
}
