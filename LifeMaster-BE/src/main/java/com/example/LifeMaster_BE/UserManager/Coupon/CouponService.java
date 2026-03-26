package com.example.LifeMaster_BE.UserManager.Coupon;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 16;

    // 1. 사용자 쿠폰 등록
    @Transactional
    public Coupon registerCoupon(Long userId, String couponCode) {
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 없음"));

        if (coupon.getCouponStatus() != CouponStatus.UNUSE) {
            throw new IllegalArgumentException("이미 등록/사용된 쿠폰");
        }

        coupon = coupon.toBuilder()
                .user(user)
                .couponStatus(CouponStatus.REGISTER)
                .build();

        return couponRepository.save(coupon);
    }

    // 2. 사용자 쿠폰 사용
    @Transactional
    public Coupon useCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 없음"));

        if (coupon.getUser() == null || !coupon.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인 쿠폰 아님");
        }

        if (coupon.getCouponStatus() != CouponStatus.REGISTER) {
            throw new IllegalArgumentException("사용 불가능한 상태");
        }

        coupon = coupon.toBuilder()
                .couponStatus(CouponStatus.USE)
                .build();

        return couponRepository.save(coupon);
    }

    // 3. 사용자 쿠폰 조회
    @Transactional(readOnly = true)
    public List<Coupon> getUserCoupons(Long userId) {
        return couponRepository.findByUserIdAndCouponStatus(userId, CouponStatus.REGISTER);
    }

    // 4. 관리자 쿠폰 생성
    @Transactional
    public Coupon createCoupon(Integer percent) {
        String code = generateUniqueCouponCode();
        Coupon coupon = Coupon.builder()
                .couponCode(code)
                .couponPercent(percent)
                .couponStatus(CouponStatus.UNUSE)
                .build();

        return couponRepository.save(coupon);
    }

    public String generateCouponCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }

    public String generateUniqueCouponCode() {
        String code;

        do {
            code = generateCouponCode();
        } while (couponRepository.existsByCouponCode(code));

        return code;
    }
}