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
}