package com.example.LifeMaster_BE.Payment;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments/googlePay")
public class GooglePayController {
    private final GooglePlayService googlePlayService;

    public GooglePayController(GooglePlayService googlePlayService) {
        this.googlePlayService = googlePlayService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyReceipt(@RequestBody ReceiptRequest request) {
        SubscriptionPurchase purchase = googlePlayService.verifyAndSavePurchase(request.getSubscriptionId(), request.getPurchaseToken());

        // 결제 상태를 getPaymentState()로 확인
        int paymentState = purchase.getPaymentState();

        // 0 = 결제 성공 상태
        if (paymentState == 0) {
            // 구독 시작 및 만료일 업데이트
            return ResponseEntity.ok("Subscription is valid until " + purchase.getExpiryTimeMillis());
        } else {
            return ResponseEntity.status(400).body("Invalid payment state");
        }
    }
}

