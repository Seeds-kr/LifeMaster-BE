package com.example.LifeMaster_BE.Admin.Payment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/payments")
public class AdminPurchaseController {

    private final AdminPurchaseService adminPurchaseService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<AdminPurchaseResponseDto>> getPurchases(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        page = Math.max(page, 0);

        if (size < 1) {
            size = 20;
        }

        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "purchaseTime")
        );

        Page<AdminPurchaseResponseDto> result;

        if (memberId != null) {
            result = adminPurchaseService.getPurchasesByMemberId(
                    memberId,
                    pageable
            );
        } else if (email != null && !email.isBlank()) {
            result = adminPurchaseService.getPurchasesByMemberEmail(
                    email.trim(),
                    pageable
            );
        } else {
            result = adminPurchaseService.getAllPurchases(pageable);
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{purchaseToken}")
    public ResponseEntity<?> deletePurchase(
            @PathVariable String purchaseToken
    ) {
        adminPurchaseService.deletePurchase(purchaseToken);
        return ResponseEntity.ok("결제 내역이 삭제되었습니다.");
    }
}