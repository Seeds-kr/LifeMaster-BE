package com.example.LifeMaster_BE.UserManager.Coupon;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.MemberSubscriptionService;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final MemberSubscriptionService memberSubscriptionService;
    private final RedissonClient redissonClient;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 16;

    // 1. 사용자 쿠폰 등록 (Redisson 분산 락)
    public Coupon registerCoupon(Long userId, String couponCode) {
        String lockKey = "lock:coupon:register:" + couponCode;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(10, -1, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("락 획득 실패: 다른 요청 처리 중");
            }

            return doRegisterCoupon(userId, couponCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public Coupon doRegisterCoupon(Long userId, String couponCode) {
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

        // 쿠폰 사용 성공 시 프리미엄 1개월 적용
        memberSubscriptionService.updateSubscription(userId, SubscriptionPlan.PREMIUM);

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
