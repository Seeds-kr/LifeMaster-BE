package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepeatDetoxService {

    private final RepeatDetoxRepository repeatDetoxRepository;
    private final RepeatDetoxSessionRepository sessionRepository;
    private final SubscriptionAccessService subscriptionAccessService;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 현재 프론트에서 받은 오늘 누적 앱 사용량
    private final Map<Long, Integer> todayUsageMap = new HashMap<>();

    // 마지막 잠금이 발생했을 당시의 누적 앱 사용량
    private final Map<Long, Integer> lastLockUsageMap = new HashMap<>();

    // 비상탈출 문구
    private final Map<Long, String> phraseMap = new HashMap<>();

    // ===================== CREATE =====================

    @Transactional
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

        List<RepeatDetoxDto.Response> list = repeatDetoxRepository.findByMember_Id(userId)
                .stream()
                .map(detox -> RepeatDetoxDto.Response.builder()
                        .id(detox.getId())
                        .lockedApp(detox.getLockedApp())
                        .sessionUsageLimit(detox.getSessionUsageLimit())
                        .lockDuration(detox.getLockDuration())
                        .dailyMaxUsageLimit(detox.getDailyMaxUsageLimit())
                        .todayUsedMinutes(todayUsageMap.getOrDefault(detox.getId(), 0))
                        .build())
                .collect(Collectors.toList());

        return RepeatDetoxDto.ListResponse.builder()
                .lockedApps(list)
                .build();
    }

    // ===================== DELETE =====================

    @Transactional
    public void deleteRepeatDetox(Long userId, Long id) {
        MemberEntity member = getMemberOrThrow(userId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        RepeatDetox detox = repeatDetoxRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repeat detox not found"));

        if (detox.getMember() == null || !detox.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("Not owner");
        }

        // 진행 중인 세션이 있다면 삭제 전에 현재까지 수행시간 확정
        closeRunningSession(detox, false);

        repeatDetoxRepository.delete(detox);

        todayUsageMap.remove(id);
        lastLockUsageMap.remove(id);
        phraseMap.remove(id);

        eventPublisher.publishEvent(
                new DetoxProgressChangedEvent(userId, LocalDate.now(KST))
        );
    }

    // ===================== LOCK STATUS =====================

    @Transactional
    public List<RepeatDetoxDto.LockStatusResponse> getLockStatus(Long userId) {
        List<RepeatDetox> detoxList = repeatDetoxRepository.findByMember_Id(userId);

        return detoxList.stream()
                .map(detox -> {
                    finishSessionIfExpired(detox);

                    boolean locked = sessionRepository
                            .findFirstByRepeatDetox_IdAndEndedAtIsNullOrderByStartedAtDesc(detox.getId())
                            .isPresent();

                    return RepeatDetoxDto.LockStatusResponse.builder()
                            .id(detox.getId())
                            .lockedApp(detox.getLockedApp())
                            .locked(locked)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===================== DETAIL =====================

    @Transactional
    public RepeatDetoxDto.DetailResponse getDetail(Long userId, Long id) {
        RepeatDetox detox = getOwnedDetox(userId, id);

        finishSessionIfExpired(detox);

        int todayUsed = todayUsageMap.getOrDefault(id, 0);

        boolean exceededDailyLimit =
                detox.getDailyMaxUsageLimit() != null &&
                        todayUsed >= detox.getDailyMaxUsageLimit();

        int remainingUnlockMinutes = sessionRepository
                .findFirstByRepeatDetox_IdAndEndedAtIsNullOrderByStartedAtDesc(id)
                .map(session -> {
                    long seconds = Duration.between(
                            LocalDateTime.now(KST),
                            session.getScheduledEndAt()
                    ).getSeconds();

                    if (seconds <= 0) return 0;

                    return (int) Math.ceil(seconds / 60.0);
                })
                .orElse(0);

        return RepeatDetoxDto.DetailResponse.builder()
                .id(detox.getId())
                .lockedApp(detox.getLockedApp())
                .todayUsedMinutes(todayUsed)
                .remainingUnlockMinutes(remainingUnlockMinutes)
                .exceededDailyLimit(exceededDailyLimit)
                .build();
    }

    // ===================== GENERATE PHRASE =====================

    public RepeatDetoxDto.PhraseResponse generatePhrase(Long userId, Long id) {
        getOwnedDetox(userId, id);

        String phrase = UUID.randomUUID()
                .toString()
                .substring(0, 6);

        phraseMap.put(id, phrase);

        return RepeatDetoxDto.PhraseResponse.builder()
                .phrase(phrase)
                .build();
    }

    // ===================== VERIFY PHRASE =====================

    @Transactional
    public boolean verifyPhrase(Long userId, Long id, String phrase) {
        RepeatDetox detox = getOwnedDetox(userId, id);

        String savedPhrase = phraseMap.get(id);

        if (savedPhrase == null || !savedPhrase.equals(phrase)) {
            return false;
        }

        Optional<RepeatDetoxSession> optional =
                sessionRepository.findFirstByRepeatDetox_IdAndEndedAtIsNullOrderByStartedAtDesc(id);

        if (optional.isPresent()) {
            RepeatDetoxSession session = optional.get();
            LocalDateTime now = LocalDateTime.now(KST);

            LocalDateTime actualEnd = now.isAfter(session.getScheduledEndAt())
                    ? session.getScheduledEndAt()
                    : now;

            int completedMinutes = calculateMinutes(session.getStartedAt(), actualEnd);

            session.setEndedAt(actualEnd);
            session.setCompletedMinutes(completedMinutes);
            session.setEscaped(true);

            sessionRepository.save(session);

            eventPublisher.publishEvent(
                    new DetoxProgressChangedEvent(userId, session.getDate())
            );
        }

        phraseMap.remove(id);

        return true;
    }

    // ===================== USAGE SYNC =====================

    @Transactional
    public void syncUsage(Long userId, Long detoxId, Integer todayUsedMinutes) {
        RepeatDetox detox = getOwnedDetox(userId, detoxId);

        if (todayUsedMinutes == null || todayUsedMinutes < 0) {
            todayUsedMinutes = 0;
        }

        LocalDate today = LocalDate.now(KST);

        // 프론트의 일일 사용량이 0으로 리셋됐다면 새 날짜로 판단
        int previousUsage = todayUsageMap.getOrDefault(detoxId, 0);

        if (todayUsedMinutes < previousUsage) {
            lastLockUsageMap.put(detoxId, 0);
        }

        todayUsageMap.put(detoxId, todayUsedMinutes);

        finishSessionIfExpired(detox);

        // 이미 잠금 중이면 새 잠금 생성 금지
        if (sessionRepository.existsByRepeatDetox_IdAndEndedAtIsNull(detoxId)) {
            return;
        }

        if (detox.getSessionUsageLimit() == null || detox.getSessionUsageLimit() <= 0) {
            return;
        }

        // 하루 최대 사용량을 넘긴 경우에도 잠금
        if (detox.getDailyMaxUsageLimit() != null &&
                detox.getDailyMaxUsageLimit() > 0 &&
                todayUsedMinutes >= detox.getDailyMaxUsageLimit()) {

            startLockSession(detox, today);
            return;
        }

        int lastLockedUsage = lastLockUsageMap.getOrDefault(detoxId, 0);
        int usageSinceLastLock = Math.max(0, todayUsedMinutes - lastLockedUsage);

        if (usageSinceLastLock >= detox.getSessionUsageLimit()) {
            startLockSession(detox, today);
            lastLockUsageMap.put(detoxId, todayUsedMinutes);
        }
    }

    // ===================== START LOCK =====================

    private void startLockSession(RepeatDetox detox, LocalDate date) {
        if (sessionRepository.existsByRepeatDetox_IdAndEndedAtIsNull(detox.getId())) {
            return;
        }

        if (detox.getLockDuration() == null || detox.getLockDuration() <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(KST);

        RepeatDetoxSession session = RepeatDetoxSession.builder()
                .member(detox.getMember())
                .repeatDetox(detox)
                .date(date)
                .startedAt(now)
                .scheduledEndAt(now.plusMinutes(detox.getLockDuration()))
                .completedMinutes(null)
                .escaped(false)
                .build();

        sessionRepository.save(session);
    }

    // ===================== NORMAL FINISH =====================

    private void finishSessionIfExpired(RepeatDetox detox) {
        Optional<RepeatDetoxSession> optional =
                sessionRepository.findFirstByRepeatDetox_IdAndEndedAtIsNullOrderByStartedAtDesc(
                        detox.getId()
                );

        if (optional.isEmpty()) return;

        RepeatDetoxSession session = optional.get();
        LocalDateTime now = LocalDateTime.now(KST);

        if (now.isBefore(session.getScheduledEndAt())) {
            return;
        }

        session.setEndedAt(session.getScheduledEndAt());
        session.setCompletedMinutes(
                calculateMinutes(session.getStartedAt(), session.getScheduledEndAt())
        );
        session.setEscaped(false);

        sessionRepository.save(session);

        eventPublisher.publishEvent(
                new DetoxProgressChangedEvent(
                        detox.getMember().getId(),
                        session.getDate()
                )
        );
    }

    // 삭제 등의 상황에서 현재까지 수행 시간 확정
    private void closeRunningSession(RepeatDetox detox, boolean escaped) {
        Optional<RepeatDetoxSession> optional =
                sessionRepository.findFirstByRepeatDetox_IdAndEndedAtIsNullOrderByStartedAtDesc(
                        detox.getId()
                );

        if (optional.isEmpty()) return;

        RepeatDetoxSession session = optional.get();
        LocalDateTime now = LocalDateTime.now(KST);

        LocalDateTime actualEnd = now.isAfter(session.getScheduledEndAt())
                ? session.getScheduledEndAt()
                : now;

        session.setEndedAt(actualEnd);
        session.setCompletedMinutes(
                calculateMinutes(session.getStartedAt(), actualEnd)
        );
        session.setEscaped(escaped);

        sessionRepository.save(session);
    }

    private int calculateMinutes(LocalDateTime start, LocalDateTime end) {
        return (int) Math.max(0, Duration.between(start, end).toMinutes());
    }

    private RepeatDetox getOwnedDetox(Long userId, Long id) {
        RepeatDetox detox = repeatDetoxRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repeat detox not found"));

        if (detox.getMember() == null || !detox.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("Not owner");
        }

        return detox;
    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}