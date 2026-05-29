package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepeatDetoxService {

    private final RepeatDetoxRepository repeatDetoxRepository;
    private final SubscriptionAccessService subscriptionAccessService;
    private final MemberRepository memberRepository;

    // detoxId -> 오늘 사용 시간(분)
    private final Map<Long, Integer> todayUsageMap = new HashMap<>();

    // detoxId -> 잠금 종료 시간
    private final Map<Long, Long> lockEndTimeMap = new HashMap<>();

    // detoxId -> 비상탈출 문구
    private final Map<Long, String> phraseMap = new HashMap<>();

    // ===================== CREATE =====================
    public void createRepeatDetox(Long userId, RepeatDetoxDto.Request request) {

        MemberEntity member = getMemberOrThrow(userId);

        subscriptionAccessService.validateFeatureAccess(
                member,
                FeatureType.Detox
        );

        RepeatDetox repeatDetox = RepeatDetox.builder()
                .lockedApp(request.getLockedApp())
                .sessionUsageLimit(request.getSessionUsageLimit())
                .lockDuration(request.getLockDuration())
                .dailyMaxUsageLimit(request.getDailyMaxUsageLimit())
                .member(member)
                .build();

        repeatDetoxRepository.save(repeatDetox);
    }

    // ===================== LIST =====================
    public RepeatDetoxDto.ListResponse getAllRepeatDetox(Long userId) {

        MemberEntity member = getMemberOrThrow(userId);

        subscriptionAccessService.validateFeatureAccess(
                member,
                FeatureType.Detox
        );

        List<RepeatDetox> detoxList =
                repeatDetoxRepository.findByMember_Id(userId);

        List<RepeatDetoxDto.Response> list = detoxList.stream()
                .map(detox -> {

                    Integer todayUsed =
                            todayUsageMap.getOrDefault(detox.getId(), 0);

                    return RepeatDetoxDto.Response.builder()
                            .id(detox.getId())
                            .lockedApp(detox.getLockedApp())
                            .sessionUsageLimit(detox.getSessionUsageLimit())
                            .lockDuration(detox.getLockDuration())
                            .dailyMaxUsageLimit(detox.getDailyMaxUsageLimit())
                            .todayUsedMinutes(todayUsed)
                            .build();
                })
                .collect(Collectors.toList());

        return RepeatDetoxDto.ListResponse.builder()
                .lockedApps(list)
                .build();
    }

    // ===================== DELETE =====================
    public void deleteRepeatDetox(Long userId, Long id) {

        MemberEntity member = getMemberOrThrow(userId);

        subscriptionAccessService.validateFeatureAccess(
                member,
                FeatureType.Detox
        );

        RepeatDetox detox = repeatDetoxRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Repeat detox not found"));

        if (detox.getMember() == null ||
                !detox.getMember().getId().equals(userId)) {

            throw new IllegalArgumentException("Not owner");
        }

        repeatDetoxRepository.delete(detox);
    }

    // ===================== LOCK STATUS =====================
    public List<RepeatDetoxDto.LockStatusResponse> getLockStatus(Long userId) {

        long now = System.currentTimeMillis();

        List<RepeatDetox> detoxList =
                repeatDetoxRepository.findByMember_Id(userId);

        return detoxList.stream()
                .map(detox -> {

                    boolean locked =
                            lockEndTimeMap.getOrDefault(
                                    detox.getId(),
                                    0L
                            ) > now;

                    return RepeatDetoxDto.LockStatusResponse.builder()
                            .id(detox.getId())
                            .lockedApp(detox.getLockedApp())
                            .locked(locked)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===================== DETAIL =====================
    public RepeatDetoxDto.DetailResponse getDetail(
            Long userId,
            Long id
    ) {

        RepeatDetox detox = repeatDetoxRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Repeat detox not found"));

        if (detox.getMember() == null ||
                !detox.getMember().getId().equals(userId)) {

            throw new IllegalArgumentException("Not owner");
        }

        int todayUsed =
                todayUsageMap.getOrDefault(id, 0);

        boolean exceededDailyLimit =
                detox.getDailyMaxUsageLimit() != null
                        && todayUsed >= detox.getDailyMaxUsageLimit();

        long now = System.currentTimeMillis();

        long lockEnd =
                lockEndTimeMap.getOrDefault(id, 0L);

        int remainingUnlockMinutes =
                (int) Math.max(0, (lockEnd - now) / 60000);

        return RepeatDetoxDto.DetailResponse.builder()
                .id(detox.getId())
                .lockedApp(detox.getLockedApp())
                .todayUsedMinutes(todayUsed)
                .remainingUnlockMinutes(remainingUnlockMinutes)
                .exceededDailyLimit(exceededDailyLimit)
                .build();
    }

    // ===================== GENERATE PHRASE =====================
    public RepeatDetoxDto.PhraseResponse generatePhrase() {

        String phrase =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 6);

        return RepeatDetoxDto.PhraseResponse.builder()
                .phrase(phrase)
                .build();
    }

    // ===================== VERIFY PHRASE =====================
    public boolean verifyPhrase(Long id, String phrase) {

        String savedPhrase = phraseMap.get(id);

        if (savedPhrase == null ||
                !savedPhrase.equals(phrase)) {

            return false;
        }

        // 잠금 즉시 해제
        lockEndTimeMap.put(id, System.currentTimeMillis());

        phraseMap.remove(id);

        return true;
    }

    // ===================== MEMBER =====================
    private MemberEntity getMemberOrThrow(Long userId) {

        return memberRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        ));
    }
}