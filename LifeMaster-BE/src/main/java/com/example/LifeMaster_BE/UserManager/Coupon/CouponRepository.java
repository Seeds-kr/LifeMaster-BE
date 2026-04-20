package com.example.LifeMaster_BE.UserManager.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponCode(String couponCode);

    List<Coupon> findByUserIdAndCouponStatus(Long userId, CouponStatus couponStatus);

    boolean existsByCouponCode(String code);

    @Query("SELECT c FROM Coupon c LEFT JOIN FETCH c.user ORDER BY c.id DESC")
    List<Coupon> findAllWithUser();

    @Query("SELECT c FROM Coupon c LEFT JOIN FETCH c.user WHERE c.couponStatus = :status ORDER BY c.id DESC")
    List<Coupon> findAllWithUserByStatus(CouponStatus status);
}