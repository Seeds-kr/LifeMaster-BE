package com.example.LifeMaster_BE.UserManager.Coupon;

import com.example.LifeMaster_BE.Config.Lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 쿠폰 Facade
 * 분산 락을 적용하고 CouponService를 호출합니다.
 */
@Service
@RequiredArgsConstructor
public class CouponFacade {

    private final CouponService couponService;

    /**
     * 쿠폰 등록 (분산 락 적용)
     */
    @DistributedLock(key = "#couponCode", waitTime = 10, leaseTime = 3)
    public Coupon registerCoupon(Long userId, String couponCode) {
        return couponService.registerCoupon(userId, couponCode);
    }

    /**
     * 쿠폰 사용
     */
    public Coupon useCoupon(Long userId, Long couponId) {
        return couponService.useCoupon(userId, couponId);
    }

    /**
     * 사용자 쿠폰 조회
     */
    public List<Coupon> getUserCoupons(Long userId) {
        return couponService.getUserCoupons(userId);
    }

    /**
     * 관리자 쿠폰 생성
     */
    public Coupon createCoupon(Integer percent) {
        return couponService.createCoupon(percent);
    }
}
