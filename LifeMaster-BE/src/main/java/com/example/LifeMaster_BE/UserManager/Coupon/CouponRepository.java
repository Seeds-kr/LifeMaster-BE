package com.example.LifeMaster_BE.UserManager.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponCode(String couponCode);

    List<Coupon> findByUserUserIdAndCouponStatus(Long userId, CouponStatus couponStatus);
}