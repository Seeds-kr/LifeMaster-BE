package com.example.LifeMaster_BE.Payment;

import lombok.Getter;
import lombok.Setter;

/**
 * Google Play 구독 검증 요청용 DTO
 */
@Getter
@Setter
public class ReceiptRequest {
    /** Google Play 구독 상품 ID */
    private String subscriptionId;

    /** Google Play 결제 토큰 */
    private String purchaseToken;

    // ✅ 컨트롤러에서 사용하는 추가 필드
    /** 상품명 (예: 프리미엄 구독) */
    private String productName;

    /** 결제 금액 (정수, 필요 시 BigDecimal 사용 가능) */
    private Integer amount;

    /** 통화 코드 (예: KRW, USD 등) */
    private String currency;
}