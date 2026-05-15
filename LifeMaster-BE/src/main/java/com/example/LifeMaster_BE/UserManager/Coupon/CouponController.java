package com.example.LifeMaster_BE.UserManager.Coupon;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    /** 1. 사용자 쿠폰 등록 */
    @PostMapping("/register")
    @Operation(summary = "쿠폰 등록", description = "쿠폰 코드를 입력하여 쿠폰을 등록합니다.")
    public Coupon registerCoupon(
            @RequestBody CouponDto.Register dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        Long memberId = user.getId();
        String email = user.getUsername(); // 기존 로직 유지

        return couponService.registerCoupon(memberId, dto.getCouponCode());
    }

    /** 2. 사용자 쿠폰 사용 */
    @PostMapping("/use")
    @Operation(summary = "쿠폰 사용", description = "등록된 쿠폰을 사용합니다.")
    public Coupon useCoupon(
            @RequestBody CouponDto.Use dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        String email = user.getUsername(); // 기존 로직 유지

        return couponService.useCoupon(memberId, dto.getCouponCode());
    }

    /** 3. 사용자 쿠폰 조회 */
    @GetMapping
    @Operation(summary = "내 쿠폰 조회", description = "사용자가 등록한 쿠폰 목록을 조회합니다.")
    public List<Coupon> getMyCoupons(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        String email = user.getUsername(); // 기존 로직 유지

        return couponService.getUserCoupons(memberId);
    }

    /** 4. 관리자 쿠폰 생성 */
    /** 4. 관리자 쿠폰 생성 */
    @PostMapping("/admin")
    @Operation(summary = "쿠폰 생성 (관리자)", description = "관리자가 새로운 쿠폰을 생성합니다.")
    public Coupon createCoupon(@RequestBody CouponDto.Create dto) {
        if (dto.getCouponType() == null) {
            throw new IllegalArgumentException("쿠폰 타입은 필수입니다.");
        }

        return switch (dto.getCouponType()) {
            case LIMIT -> couponService.createLimitCoupon(dto.getCouponPercent());
            case UNLIMIT -> couponService.createUnlimitCoupon(dto.getCouponPercent());
        };
    }

    /** 5. 관리자 쿠폰 삭제 */
    @DeleteMapping("/admin/{couponId}")
    @Operation(summary = "쿠폰 삭제 (관리자)", description = "관리자가 쿠폰을 삭제합니다.")
    public void deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
    }
}