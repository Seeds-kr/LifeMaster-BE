package com.example.LifeMaster_BE.UserManager.Coupon;

import com.example.LifeMaster_BE.Admin.Coupon.AdminCouponStatusResponse;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.MemberSubscriptionService;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final MemberSubscriptionService memberSubscriptionService;
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

    // 2. 사용자 쿠폰 사용 (코드 입력 방식)
    @Transactional
    public Coupon useCoupon(Long userId, String couponCode) {
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 없음"));

        if (coupon.getUser() == null || !coupon.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인 쿠폰 아님");
        }

        if (coupon.getCouponStatus() != CouponStatus.REGISTER) {
            throw new IllegalArgumentException("사용 불가능한 상태");
        }

        if (coupon.getCouponType() == null) {
            throw new IllegalArgumentException("쿠폰 타입 정보가 없습니다.");
        }

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 이미 영구 프리미엄이면 LIMIT 쿠폰 사용 불가
        if (coupon.getCouponType() == CouponType.LIMIT
                && member.getSubscriptionPlan() == SubscriptionPlan.PREMIUM
                && member.getSubscriptionExpirationDate() != null
                && member.getSubscriptionExpirationDate().equals(LocalDate.of(9999, 12, 31))) {
            throw new IllegalArgumentException("무제한 프리미엄 이용 중에는 기간제 쿠폰을 사용할 수 없습니다.");
        }

        switch (coupon.getCouponType()) {
            case LIMIT -> memberSubscriptionService.updateSubscription13Month(userId, SubscriptionPlan.PREMIUM);
            case UNLIMIT -> memberSubscriptionService.updateSubscriptionPermanent(userId, SubscriptionPlan.PREMIUM);
            default -> throw new IllegalArgumentException("지원하지 않는 쿠폰 타입입니다.");
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
    public Coupon createLimitCoupon(Integer percent) {
        return createCoupon(percent, CouponType.LIMIT);
    }

    @Transactional
    public Coupon createUnlimitCoupon(Integer percent) {
        return createCoupon(percent, CouponType.UNLIMIT);
    }

    private Coupon createCoupon(Integer percent, CouponType couponType) {
        String code = generateUniqueCouponCode();

        Coupon coupon = Coupon.builder()
                .couponCode(code)
                .couponPercent(percent)
                .couponType(couponType)
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

    // 5. 관리자 - 전체 쿠폰 상태 조회
    public List<AdminCouponStatusResponse> getAllCouponsForAdmin() {
        return couponRepository.findAll(Sort.by(Sort.Direction.ASC, "couponId"))
                .stream()
                .map(AdminCouponStatusResponse::from)
                .toList();
    }

    // 6. 관리자 - 상태별 쿠폰 조회
    public List<AdminCouponStatusResponse> getCouponsForAdminByStatus(CouponStatus status) {
        return couponRepository.findByCouponStatus(
                        status,
                        Sort.by(Sort.Direction.ASC, "couponId")
                )
                .stream()
                .map(AdminCouponStatusResponse::from)
                .toList();
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        if (coupon.getCouponStatus() != CouponStatus.UNUSE) {
            throw new IllegalStateException("등록되었거나 사용된 쿠폰은 삭제할 수 없습니다.");
        }

        couponRepository.delete(coupon);
    }
}