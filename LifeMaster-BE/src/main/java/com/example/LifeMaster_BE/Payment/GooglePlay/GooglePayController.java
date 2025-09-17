package com.example.LifeMaster_BE.Payment.GooglePlay;

import com.example.LifeMaster_BE.Payment.ReceiptRequest;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/googlePay")
@Tag(name = "Google Play API", description = "Google Play 구독 검증 및 저장 API")
public class GooglePayController {

    private final GooglePlayService googlePlayService;
    private final Login login;

    @Operation(
            summary = "구독 영수증 검증 & 저장",
            description = "Google Play 구독 영수증을 검증하고 DB(PurchaseEntity)에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "구독 유효",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "구독 무효/검증 실패",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "로그인 필요")
            }
    )
    @PostMapping("/verify")
    public ResponseEntity<?> verifyReceipt(
            @Parameter(description = "구독ID/토큰 등 검증 요청 본문", required = true)
            @RequestBody ReceiptRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    ) {
        // 1) 로그인 체크
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        // 2) member 스텁 (ID만 필요)
        MemberEntity member = new MemberEntity();
        member.setId(user.getId());

        // 3) 선택값(productName/amount/currency)이 Request에 있다면 사용, 없으면 null 허용
        String productName = safe(request::getProductName); // 없으면 null
        Integer amount     = safe(request::getAmount);      // 없으면 null
        String currency    = safe(request::getCurrency);    // 없으면 null

        // 4) 검증 + 저장 (멱등성: 서비스에서 purchaseToken 중복 저장 방지)
        SubscriptionPurchase purchase = googlePlayService.verifyAndSavePurchase(
                request.getSubscriptionId(),
                request.getPurchaseToken(),
                member,
                productName,
                amount,
                currency
        );

        // 5) 유효성 판정: expiryTimeMillis 기준
        Long expiryMs = purchase.getExpiryTimeMillis();
        boolean valid = (expiryMs != null) && (expiryMs > System.currentTimeMillis());

        if (valid) {
            return ResponseEntity.ok("Subscription is valid until " + expiryMs);
        } else {
            // 보조 지표로 paymentState, cancelReason 등 로그 참고
            Integer paymentState = purchase.getPaymentState(); // null 가능
            Integer cancelReason = purchase.getCancelReason(); // null 가능
            String detail = String.format("Invalid subscription. paymentState=%s, cancelReason=%s, expiry=%s",
                    String.valueOf(paymentState), String.valueOf(cancelReason), String.valueOf(expiryMs));
            return ResponseEntity.badRequest().body(detail);
        }
    }

    // NPE 방지용 헬퍼
    private static <T> T safe(SupplierEx<T> s) {
        try { return s.get(); } catch (Throwable t) { return null; }
    }
    @FunctionalInterface
    private interface SupplierEx<T> { T get(); }
}
