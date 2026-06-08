package com.example.LifeMaster_BE.UserManager.Member.Subscription;

import com.example.LifeMaster_BE.Admin.Premium.PremiumMemberAdminDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentDto;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentEntity;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentRepository;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.LifeMaster_BE.Admin.Premium.AdminPremiumGrantRequest;
import com.example.LifeMaster_BE.Admin.Premium.NonPremiumMemberAdminDto;
import com.example.LifeMaster_BE.Admin.Premium.PremiumGrantType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MemberSubscriptionService {
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public MemberSubscriptionService(MemberRepository memberRepository, PaymentRepository paymentRepository) {
        this.memberRepository = memberRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * 사용자의 요금제 변경
     */
    @Transactional
    public MemberEntity updateSubscription(Long memberId, SubscriptionPlan newPlan) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다."));

        LocalDate now = LocalDate.now();

        LocalDate startDate;
        LocalDate currentExpirationDate = member.getSubscriptionExpirationDate();

        if (member.getSubscriptionPlan() == SubscriptionPlan.PREMIUM
                && currentExpirationDate != null
                && !currentExpirationDate.isBefore(now)) {

            // 🔥 기존 기간 남아있으면 이어서 연장
            startDate = currentExpirationDate.plusDays(1);

        } else {
            // 🔥 무료 or 만료 → 오늘부터 시작
            startDate = now;
        }

        // 🔥 1개월 연장
        LocalDate expirationDate = startDate.plusMonths(1).minusDays(1);

        member.updateSubscription(newPlan, now, expirationDate);
        return memberRepository.save(member);
    }

    @Transactional
    public MemberEntity updateSubscription13Month(Long memberId, SubscriptionPlan newPlan) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다."));

        LocalDate now = LocalDate.now();

        LocalDate startDate;
        LocalDate currentExpirationDate = member.getSubscriptionExpirationDate();

        if (member.getSubscriptionPlan() == SubscriptionPlan.PREMIUM
                && currentExpirationDate != null
                && !currentExpirationDate.isBefore(now)) {

            // 아직 남아있으면 이어서 연장
            startDate = currentExpirationDate.plusDays(1);

        } else {
            // 무료 or 만료됨 → 오늘부터
            startDate = now;
        }

        LocalDate expirationDate = startDate.plusMonths(13).minusDays(1);

        member.updateSubscription(newPlan, now, expirationDate);
        return memberRepository.save(member);
    }

    /**
     * 어드민용 - 구독 기한 1개월 추가
     *
     * FREE / 만료 회원이면 오늘부터 1개월 부여
     * PREMIUM / 아직 유효한 회원이면 기존 만료일 다음 날부터 1개월 연장
     * 영구 회원이면 연장하지 않음
     */
    @Transactional
    public void extendPremiumOneMonthForAdmin(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다."));

        LocalDate now = LocalDate.now();
        LocalDate currentExpirationDate = member.getSubscriptionExpirationDate();

        // 영구 프리미엄 계정이면 연장 불필요
        if (currentExpirationDate != null
                && currentExpirationDate.equals(LocalDate.of(9999, 12, 31))) {
            throw new IllegalStateException("영구 프리미엄 회원은 구독 기한을 연장할 수 없습니다.");
        }

        LocalDate startDate;

        if (member.getSubscriptionPlan() == SubscriptionPlan.PREMIUM
                && currentExpirationDate != null
                && !currentExpirationDate.isBefore(now)) {

            // 기존 구독이 아직 유효하면 만료일 다음 날부터 연장
            startDate = currentExpirationDate.plusDays(1);

        } else {
            // FREE 또는 만료 회원이면 오늘부터 시작
            startDate = now;
        }

        LocalDate expirationDate = startDate.plusMonths(1).minusDays(1);

        member.updateSubscription(
                SubscriptionPlan.PREMIUM,
                now,
                expirationDate
        );

        memberRepository.save(member);
    }

    /**
     * 사용자의 영구적 요금제 변경
     */
    @Transactional
    public MemberEntity updateSubscriptionPermanent(Long memberId, SubscriptionPlan newPlan) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다."));

        LocalDate now = LocalDate.now();
        LocalDate expirationDate = LocalDate.of(9999, 12, 31); // 사실상 영구

        member.updateSubscription(newPlan, now, expirationDate);
        return memberRepository.save(member);
    }
    /**
     * 결제 내역 추가
     */
    @Transactional
    public PaymentEntity addPayment(Long memberId, double amount) {
        Optional<MemberEntity> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다.");
        }

        MemberEntity member = optionalMember.get();

        PaymentEntity payment = new PaymentEntity();
        payment.setMember(member);
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PAID);

        member.getPayments().add(payment);
        member.setPaymentStatus(PaymentStatus.PAID);

        paymentRepository.save(payment);
        memberRepository.save(member);

        return payment;
    }

    /**
     * 사용자의 요금제 및 결제 상태 조회
     */
    public SubscriptionInfoDto getSubscriptionInfo(Long memberId) {
        Optional<MemberEntity> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다.");
        }

        MemberEntity member = optionalMember.get();
        return new SubscriptionInfoDto(
                member.getSubscriptionPlan(),
                member.getLastPaymentDate(),
                member.getExpirationDate(),
                member.getPaymentStatus()
        );
    }

    /**
     * 사용자 별 결제 내역 조회
     */
    public List<PaymentDto> getPaymentHistory(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다."));

        return member.getPayments().stream()
                .map(PaymentDto::from) // DTO 변환
                .toList();
    }

    /**
     * 어드민용 - 전체 프리미엄 유저의 유효기간 조회
     */
    @Transactional(readOnly = true)
    public List<PremiumMemberAdminDto> getAllPremiumMembersForAdmin() {
        return memberRepository.findAllBySubscriptionPlan(SubscriptionPlan.PREMIUM)
                .stream()
                .map(PremiumMemberAdminDto::from)
                .toList();
    }

    /**
     * 어드민용 - 일반 회원 조회
     */
    @Transactional(readOnly = true)
    public Page<NonPremiumMemberAdminDto> getNonPremiumMembersForAdmin(
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return memberRepository
                .findAllBySubscriptionPlan(SubscriptionPlan.FREE, pageable)
                .map(NonPremiumMemberAdminDto::from);
    }

    /**
     * 어드민용 - 프리미엄 권한 부여
     */
    @Transactional
    public void grantPremiumForAdmin(Long memberId, PremiumGrantType grantType) {
        switch (grantType) {
            case ONE_MONTH ->
                    updateSubscription(memberId, SubscriptionPlan.PREMIUM);

            case THIRTEEN_MONTHS ->
                    updateSubscription13Month(memberId, SubscriptionPlan.PREMIUM);

            case PERMANENT ->
                    updateSubscriptionPermanent(memberId, SubscriptionPlan.PREMIUM);
        }
    }

    /**
     * 어드민용 - 프리미엄 권한 회수
     */
    @Transactional
    public void revokePremiumForAdmin(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다."));

        member.revokePremium();

        memberRepository.save(member);
    }
}