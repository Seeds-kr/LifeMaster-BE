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

    // ===== 상태 저장 (임시, 실제는 Redis/DB 권장) =====
    private final Map<Long, Integer> todayUsageMap = new HashMap<>();   // detoxId -> 사용시간(분)
    private final Map<Long, Long> lockEndTimeMap = new HashMap<>();     // detoxId -> lock end timestamp
    private final Map<Long, String> phraseMap = new HashMap<>();        // detoxId -> phrase

    // ===================== CREATE =====================
    public void createRepeatDetox(Long userId, RepeatDetoxDto.Request request) {

        MemberEntity member = getMemberOrThrow(userId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

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
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        List<RepeatDetoxDto.Response> list = repeatDetoxRepository.findAll()
                .stream()
                .filter(d -> d.getMember().getId().equals(userId))
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

    // ===================== DELETE =====================
    public void deleteRepeatDetox(Long userId, Long id) {

        MemberEntity member = getMemberOrThrow(userId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        RepeatDetox detox = repeatDetoxRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found"));

        if (!detox.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("not owner");
        }

        repeatDetoxRepository.deleteById(id);
    }

    // ===================== LOCK STATUS =====================
    public List<RepeatDetoxDto.LockStatusResponse> getLockStatus(Long userId) {

        long now = System.currentTimeMillis();

        return repeatDetoxRepository.findAll()
                .stream()
                .filter(d -> d.getMember().getId().equals(userId))
                .map(d -> {

                    boolean locked = lockEndTimeMap.getOrDefault(d.getId(), 0L) > now;

                    return RepeatDetoxDto.LockStatusResponse.builder()
                            .id(d.getId())
                            .lockedApp(d.getLockedApp())
                            .locked(locked)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===================== DETAIL =====================
    public RepeatDetoxDto.DetailResponse getDetail(Long userId, Long id) {

        RepeatDetox detox = repeatDetoxRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found"));

        if (!detox.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("not owner");
        }

        int todayUsed = todayUsageMap.getOrDefault(id, 0);

        boolean exceeded = detox.getDailyMaxUsageLimit() != null
                && todayUsed >= detox.getDailyMaxUsageLimit();

        long now = System.currentTimeMillis();
        long lockEnd = lockEndTimeMap.getOrDefault(id, 0L);

        int remainingLock = (int) Math.max(0, (lockEnd - now) / 60000);

        return RepeatDetoxDto.DetailResponse.builder()
                .id(id)
                .lockedApp(detox.getLockedApp())
                .todayUsedMinutes(todayUsed)
                .remainingUnlockMinutes(remainingLock)
                .exceededDailyLimit(exceeded)
                .build();
    }

    // ===================== PHRASE 생성 =====================
    public RepeatDetoxDto.PhraseResponse generatePhrase() {

        String phrase = UUID.randomUUID().toString().substring(0, 6);

        // demo: 첫 detox 기준 (실전은 user+detox 매핑 필요)
        phraseMap.put(0L, phrase);

        return RepeatDetoxDto.PhraseResponse.builder()
                .phrase(phrase)
                .build();
    }

    // ===================== PHRASE 검증 =====================
    public boolean verifyPhrase(Long id, String phrase) {

        String saved = phraseMap.get(id);

        if (saved == null || !saved.equals(phrase)) {
            return false;
        }

        long now = System.currentTimeMillis();
        long lockEnd = lockEndTimeMap.getOrDefault(id, 0L);

        // 잠금 즉시 해제 (핵심)
        lockEndTimeMap.put(id, now);

        // 마지막 구간 처리
        RepeatDetox detox = repeatDetoxRepository.findById(id)
                .orElseThrow();

        int used = todayUsageMap.getOrDefault(id, 0);

        if (detox.getDailyMaxUsageLimit() != null
                && used >= detox.getDailyMaxUsageLimit()) {
            todayUsageMap.put(id, used);
        }

        phraseMap.remove(id);

        return true;
    }

    // ===================== MEMBER =====================
    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}