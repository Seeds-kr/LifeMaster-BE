package com.example.LifeMaster_BE.Payment.NaverPay;

import com.example.LifeMaster_BE.Payment.PaymentRequest;
import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/NaverPay")
@Tag(name = "NaverPay API", description = "NaverPay 결제 생성/완료/조회 API")
public class NaverPayController {

    private final NaverPayService purchaseService;
    private final Login login;

    @Operation(
            summary = "네이버페이 결제 생성",
            description = "네이버페이에 결제 요청을 생성하고 paymentId를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 생성 성공",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "401", description = "로그인 필요")
            }
    )
    @PostMapping("/createPayment")
    public ResponseEntity<Map<String, String>> createPayment(
            @Parameter(description = "결제 요청 정보(주문ID/상품명/금액 등)", required = true)
            @RequestBody PaymentRequest paymentRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    ) throws Exception {

        // 로그인 체크(필요 없으면 제거 가능)
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return (ResponseEntity<Map<String, String>>) loginCheck;

        String paymentId = purchaseService.createPayment(paymentRequest);

        Map<String, String> response = new HashMap<>();
        response.put("paymentId", paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "네이버페이 결제 완료",
            description = "구매 승인 완료 콜백(또는 프런트 전송)으로 결제를 검증하고 DB에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 완료 처리됨",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "검증 실패"),
                    @ApiResponse(responseCode = "401", description = "로그인 필요")
            }
    )
    @PostMapping("/complete")
    public ResponseEntity<String> completePayment(
            @Parameter(description = "주문 ID", required = true) @RequestParam String orderId,
            @Parameter(description = "결제 토큰", required = true) @RequestParam String purchaseToken,
            // 아래 3개는 프런트에서 결제 생성 당시 사용한 값 전달(선택)
            @Parameter(description = "상품명(선택)") @RequestParam(required = false) String productName,
            @Parameter(description = "결제 금액(선택)") @RequestParam(required = false) Integer amount,
            @Parameter(description = "통화(선택, 기본 KRW)") @RequestParam(required = false) String currency,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    ) {
        // 로그인 체크
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return (ResponseEntity<String>) loginCheck;

        // member: ID만 세팅해도 충분(서비스에서 FK로 저장)
        MemberEntity member = new MemberEntity();
        member.setId(user.getId());

        purchaseService.completePayment(orderId, purchaseToken, member, productName, amount, currency);
        return ResponseEntity.ok("결제 완료 처리됨");
    }

    @Operation(
            summary = "네이버페이 결제 단건 조회",
            description = "purchaseToken으로 결제 내역을 단건 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = PurchaseEntity.class))),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 토큰")
            }
    )
    @GetMapping("/status")
    public ResponseEntity<PurchaseEntity> getPaymentStatus(
            @Parameter(description = "결제 토큰", required = true) @RequestParam String purchaseToken
    ) {
        PurchaseEntity purchase = purchaseService.getPurchaseInfo(purchaseToken);
        return ResponseEntity.ok(purchase);
    }
}
