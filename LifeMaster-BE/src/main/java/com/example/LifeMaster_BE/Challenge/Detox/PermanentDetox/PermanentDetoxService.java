package com.example.LifeMaster_BE.Challenge.Detox.PermanentDetox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PermanentDetoxService {

    private final PermanentDetoxRepository permanentDetoxRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionAccessService subscriptionAccessService;

    public PermanentDetoxResponseDTO createPermanentDetox(Long loginMemberId, PermanentDetoxRequestDTO requestDTO) {

        MemberEntity member = getMemberOrThrow(loginMemberId);

        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        if (permanentDetoxRepository.existsByMember_Id(loginMemberId)) {
            throw new IllegalArgumentException("이미 영구 잠금 디톡스가 존재합니다.");
        }

        PermanentDetoxEntity entity = new PermanentDetoxEntity();
        entity.setMember(member);
        entity.setLockedApps(requestDTO.getLockedApps());

        PermanentDetoxEntity saved = permanentDetoxRepository.save(entity);
        return new PermanentDetoxResponseDTO(saved.getLockedApps());
    }

    @Transactional(readOnly = true)
    public PermanentDetoxResponseDTO getPermanentDetox(Long loginMemberId) {

        MemberEntity member = getMemberOrThrow(loginMemberId);

        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        PermanentDetoxEntity entity = permanentDetoxRepository.findByMember_Id(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("영구 잠금 디톡스가 존재하지 않습니다."));

        return new PermanentDetoxResponseDTO(entity.getLockedApps());
    }

    public PermanentDetoxResponseDTO updatePermanentDetox(Long loginMemberId, PermanentDetoxRequestDTO requestDTO) {

        MemberEntity member = getMemberOrThrow(loginMemberId);

        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        PermanentDetoxEntity entity = permanentDetoxRepository.findByMember_Id(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("영구 잠금 디톡스가 존재하지 않습니다."));

        entity.setLockedApps(requestDTO.getLockedApps());

        PermanentDetoxEntity updated = permanentDetoxRepository.save(entity);
        return new PermanentDetoxResponseDTO(updated.getLockedApps());
    }

    public void deletePermanentDetox(Long loginMemberId) {

        MemberEntity member = getMemberOrThrow(loginMemberId);

        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        PermanentDetoxEntity entity = permanentDetoxRepository.findByMember_Id(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("영구 잠금 디톡스가 존재하지 않습니다."));

        permanentDetoxRepository.delete(entity);
    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}
