package com.example.LifeMaster_BE.Payment.NaverPay;

import com.example.LifeMaster_BE.Payment.PaymentRequest;
import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments/NaverPay")
public class NaverPayController {

    @Autowired
    private NaverPayService purchaseService;

    // 1. 결제 요청
    @PostMapping("/createPayment")
    public ResponseEntity<Map<String, String>> createPayment(@RequestBody PaymentRequest paymentRequest) throws Exception {
        // 결제 생성 로직
        String paymentId = purchaseService.createPayment(paymentRequest);

        Map<String, String> response = new HashMap<>();
        response.put("paymentId", paymentId);
        return ResponseEntity.ok(response);
    }

    // 2. 결제 완료 콜백
    @PostMapping("/complete")
    public ResponseEntity<String> completePayment(@RequestParam String orderId, @RequestParam String purchaseToken) {
        purchaseService.completePayment(orderId, purchaseToken);
        return ResponseEntity.ok("결제 완료 처리됨");
    }

    // 3. 결제 조회
    @GetMapping("/status")
    public ResponseEntity<PurchaseEntity> getPaymentStatus(@RequestParam String purchaseToken) {
        PurchaseEntity purchase = purchaseService.getPurchaseInfo(purchaseToken);
        return ResponseEntity.ok(purchase);
    }
}
