package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepeatDetoxService {

    private final RepeatDetoxRepository repeatDetoxRepository;
    private final SubscriptionAccessService subscriptionAccessService;
    private final MemberRepository memberRepository;

    // 반복 잠금 생성
    public void createRepeatDetox(Long userId, RepeatDetoxDto.Request request) {

        MemberEntity member = getMemberOrThrow(userId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        RepeatDetox repeatDetox = RepeatDetox.builder()
                .lockedApp(request.getLockedApp())
                .sessionUsageLimit(request.getSessionUsageLimit())
                .lockDuration(request.getLockDuration())
                .dailyMaxUsageLimit(request.getDailyMaxUsageLimit())
                .build();

        repeatDetoxRepository.save(repeatDetox);
    }

    // 전체 반복 잠금 조회
    public RepeatDetoxDto.ListResponse getAllRepeatDetox(Long userId) {

        MemberEntity member = getMemberOrThrow(userId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        List<RepeatDetoxDto.Response> list =
                repeatDetoxRepository.findAll()
                        .stream()
                        .map(detox -> RepeatDetoxDto.Response.builder()
                                .id(detox.getId())
                                .lockedApp(detox.getLockedApp())
                                .sessionUsageLimit(detox.getSessionUsageLimit())
                                .lockDuration(detox.getLockDuration())
                                .dailyMaxUsageLimit(detox.getDailyMaxUsageLimit())
                                .build())
                        .collect(Collectors.toList());

        return RepeatDetoxDto.ListResponse.builder()
                .lockedApps(list)
                .build();
    }

    // 반복 잠금 삭제
    public void deleteRepeatDetox(Long userId, Long id) {

        MemberEntity member = getMemberOrThrow(userId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        repeatDetoxRepository.deleteById(id);
    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}