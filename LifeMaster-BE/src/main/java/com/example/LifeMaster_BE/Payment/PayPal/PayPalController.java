package com.example.LifeMaster_BE.Payment.PayPal;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/paypal")
public class PayPalController {

    private final PayPalService payPalService;
    private final Login login;

    // 주문 생성 및 승인 URL 반환
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        Map<String, String> result = payPalService.createOrder();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/capture/{orderId}")
    public ResponseEntity<?> capture(@PathVariable("orderId") String orderId, @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        // MemberEntity 객체를 새로 생성하고 값 설정
        MemberEntity member = new MemberEntity();
        member.setId(user.getId());
        member.setPassword(user.getPassword());
        member.setEmail(user.getEmail());
        member.setNickname(user.getNickname());

        String result = payPalService.captureOrder(orderId,member);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/purchaseLog")
    public ResponseEntity<?> getMyPurchases(@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        // MemberEntity 객체를 새로 생성하고 값 설정
        MemberEntity member = new MemberEntity();
        member.setId(user.getId());
        member.setPassword(user.getPassword());
        member.setEmail(user.getEmail());
        member.setNickname(user.getNickname());

        List<PurchaseEntity> purchases = payPalService.getPurchasesByMember(member);
        return ResponseEntity.ok(purchases);
    }
}


