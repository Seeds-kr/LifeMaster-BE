package com.example.LifeMaster_BE.UserManager.Member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/{id}")
public class MemberSubscriptionController {
    private final MemberSubscriptionService subscriptionService;

    public MemberSubscriptionController(MemberSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * 사용자의 요금제 및 결제 상태 조회
     */
    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionInfoDto> getSubscriptionInfo(@PathVariable("id") Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionInfo(id));
    }

    /**
     * 사용자의 요금제 변경 (PUT 방식 사용)
     */
    @PutMapping("/subscription")
    public ResponseEntity<MemberEntity> updateSubscription(
            @PathVariable("id") Long id,
            @RequestParam("plan") SubscriptionPlan plan) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, plan));
    }

    /**
     * 사용자의 결제 내역 추가
     */
    @PostMapping("/payment")
    public ResponseEntity<PaymentEntity> addPayment(
            @PathVariable("id") Long id,
            @RequestParam("amount") double amount) {
        return ResponseEntity.ok(subscriptionService.addPayment(id, amount));
    }

    /**
     * 사용자 별 결제 내역 조회
     */
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDto>> getPaymentHistory(@PathVariable("id") Long id) {
        return ResponseEntity.ok(subscriptionService.getPaymentHistory(id));
    }
}
