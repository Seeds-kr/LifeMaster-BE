package com.example.LifeMaster_BE.UserManager.Coupon;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponCode(String couponCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.couponCode = :couponCode")
    Optional<Coupon> findByCouponCodeForUpdate(@Param("couponCode") String couponCode);

    List<Coupon> findByUserIdAndCouponStatus(Long userId, CouponStatus couponStatus);

    boolean existsByCouponCode(String code);
}