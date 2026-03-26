package com.example.LifeMaster_BE.UserManager.Member.Subscription;

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
        Optional<MemberEntity> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다.");
        }

        MemberEntity member = optionalMember.get();
        LocalDate now = LocalDate.now();
        LocalDate expirationDate = now.plusMonths(1); // 1개월 후 만료

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
}