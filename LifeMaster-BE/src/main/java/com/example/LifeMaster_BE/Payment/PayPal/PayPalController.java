package com.example.LifeMaster_BE.Payment.PayPal;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Payment.PurchaseDto;
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

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/paypal")
@Tag(name = "PayPal API", description = "PayPal 결제 처리 및 결제 내역 조회 API")
public class PayPalController {

    private final PayPalService payPalService;
    private final Login login;

    @Operation(
            summary = "PayPal 주문 생성",
            description = "PayPal에 새 주문을 생성하고 사용자 승인 URL을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주문 생성 성공",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패 또는 로그인 필요")
            }
    )
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {

        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Map<String, String> result = payPalService.createOrder();
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "PayPal 결제 캡처",
            description = "승인 완료된 주문(orderId)을 캡처하고 DB에 결제 정보를 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 캡처 및 저장 성공",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "결제 캡처 실패"),
                    @ApiResponse(responseCode = "401", description = "인증 실패 또는 로그인 필요")
            }
    )
    @PostMapping("/capture/{orderId}")
    public ResponseEntity<?> capture(
            @Parameter(description = "PayPal 주문 ID", required = true) @PathVariable String orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {

        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        MemberEntity member = buildMemberFromUser(user);
        String result = payPalService.captureOrder(orderId, member);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "내 결제 내역 조회",
            description = "로그인한 사용자의 결제 내역(모든 결제원)을 최신순으로 조회합니다. 결제 관련 필드만 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 내역 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PurchaseDto.class)))),
                    @ApiResponse(responseCode = "401", description = "인증 실패 또는 로그인 필요")
            }
    )
    @GetMapping("/purchaseLog")
    public ResponseEntity<?> getMyPurchases(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {

        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        MemberEntity member = buildMemberFromUser(user);
        List<PurchaseEntity> purchases = payPalService.getPurchasesByMember(member);

        // ✅ Entity → DTO 매핑
        List<PurchaseDto> dtoList = purchases.stream()
                .map(PurchaseDto::from)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    /** 로그인 사용자로부터 최소 MemberEntity 스텁 생성 (ID만 사용) */
    private MemberEntity buildMemberFromUser(CustomUserDetails user) {
        MemberEntity member = new MemberEntity();
        member.setId(user.getId());
        return member;
    }
}
