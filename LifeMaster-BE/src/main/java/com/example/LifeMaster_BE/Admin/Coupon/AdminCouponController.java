package com.example.LifeMaster_BE.Admin.Coupon;

import com.example.LifeMaster_BE.UserManager.Coupon.CouponService;
import com.example.LifeMaster_BE.UserManager.Coupon.CouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    private final CouponService couponService;

    // 전체 쿠폰 조회
    @GetMapping
    public ResponseEntity<List<AdminCouponStatusResponse>> getAllCoupons(
            @RequestParam(required = false) CouponStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(couponService.getCouponsForAdminByStatus(status));
        }
        return ResponseEntity.ok(couponService.getAllCouponsForAdmin());
    }
}
