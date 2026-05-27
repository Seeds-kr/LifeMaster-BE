package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TimeDetoxService {

    private final TimeDetoxRepository repository;
    private final RandomPhraseProvider randomPhraseProvider;
    private final MemberRepository memberRepository;
    private final DetoxVerificationRepository detoxVerificationRepository;
    private final SubscriptionAccessService subscriptionAccessService;
    private final TimeDetoxDailyDisableRepository dailyDisableRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private String currentRandomPhrase;

    public void updateRandomPhrase() {
        this.currentRandomPhrase = randomPhraseProvider.getRandomPhrase();
    }

    public TimeDetoxDto createSchedule(TimeDetoxDto dto, Long memberId) {

        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        TimeDetoxEntity entity = new TimeDetoxEntity();
        entity.setCycle(dto.getCycle());
        entity.setDay(dto.getDay());

        entity.setStartTime(LocalTime.parse(dto.getStartTime(), HH_MM));
        entity.setEndTime(LocalTime.parse(dto.getEndTime(), HH_MM));

        entity.setLockedApps(parseLockedApps(dto.getLockedApps()));

        entity.setActive(true);
        entity.setMemberId(memberId);

        TimeDetoxEntity savedEntity = repository.save(entity);

        return convertToDTO(savedEntity);
    }

    private List<String> parseLockedApps(String lockedAppsRaw) {

        if (lockedAppsRaw == null || lockedAppsRaw.isBlank()) {
            return Collections.emptyList();
        }

        String s = lockedAppsRaw.trim();

        try {
            if (s.startsWith("[")) {
                return objectMapper.readValue(s, new TypeReference<List<String>>() {});
            }

            return List.of(s.split("\\s*,\\s*"));

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "lockedApps 형식이 올바르지 않습니다. JSON 배열 문자열 또는 콤마 구분 문자열을 사용하세요.",
                    e
            );
        }
    }

    private TimeDetoxDto convertToDTO(TimeDetoxEntity entity) {

        TimeDetoxDto dto = new TimeDetoxDto();

        dto.setId(entity.getId());
        dto.setCycle(entity.getCycle());
        dto.setDay(entity.getDay());

        dto.setStartTime(
                entity.getStartTime() != null
                        ? entity.getStartTime().format(HH_MM)
                        : null
        );

        dto.setEndTime(
                entity.getEndTime() != null
                        ? entity.getEndTime().format(HH_MM)
                        : null
        );

        try {
            dto.setLockedApps(objectMapper.writeValueAsString(entity.getLockedApps()));
        } catch (Exception e) {
            dto.setLockedApps("[]");
        }

        dto.setDisabledToday(false);

        return dto;
    }

    private boolean isDisabledToday(Long memberId, Long timeDetoxId, LocalDate today) {
        return dailyDisableRepository.existsByMember_IdAndTimeDetox_IdAndDisabledDate(
                memberId,
                timeDetoxId,
                today
        );
    }

    public TimeDetoxEntity getScheduleById(Long memberId, Long id) {

        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        return repository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Schedule not found or no permission")
                );
    }

    public TimeDetoxEntity updateSchedule(Long memberId, Long id, TimeDetoxEntity updatedSchedule) {

        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        TimeDetoxEntity schedule = repository
                .findByIdAndMember_Id(id, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Schedule not found or no permission")
                );

        schedule.setCycle(updatedSchedule.getCycle());
        schedule.setDay(updatedSchedule.getDay());
        schedule.setStartTime(updatedSchedule.getStartTime());
        schedule.setEndTime(updatedSchedule.getEndTime());
        schedule.setLockedApps(updatedSchedule.getLockedApps());

        return repository.save(schedule);
    }

    public void deleteSchedule(Long memberId, Long id) {

        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        TimeDetoxEntity schedule = repository
                .findByIdAndMember_Id(id, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("해당 스케줄이 없거나 삭제 권한이 없습니다.")
                );

        repository.delete(schedule);
    }

    public TimeDetoxEntity toggleActivation(
            Long memberId,
            Long id,
            String currentDay,
            LocalTime currentTime
    ) {
        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        TimeDetoxEntity schedule = repository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Schedule not found or no permission")
                );

        if (!schedule.isActive()) {
            schedule.setActive(true);
        } else {
            boolean inTimeRange =
                    !currentTime.isBefore(schedule.getStartTime()) &&
                            !currentTime.isAfter(schedule.getEndTime());

            boolean isMatchingDay =
                    schedule.getDay() != null &&
                            schedule.getDay().equalsIgnoreCase(currentDay);

            if (isMatchingDay && inTimeRange) {
                throw new IllegalStateException(
                        "현재 시간이 디톡스 활성화 시간 범위에 포함되어 있어 비활성화할 수 없습니다."
                );
            }

            schedule.setActive(false);
        }

        return repository.save(schedule);
    }

    public String generateRandomPhrase(Long memberId) {

        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        String phrase = randomPhraseProvider.getRandomPhrase();

        /*
         * member_id가 UNIQUE인 구조이므로,
         * 새 row를 계속 만들면 안 되고 기존 row를 재사용해야 한다.
         */
        DetoxVerificationEntity token = detoxVerificationRepository
                .findByMember_Id(memberId)
                .orElseGet(() ->
                        DetoxVerificationEntity.create(member, phrase, null)
                );

        token.setPhrase(phrase);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(null);
        token.setUsed(false);

        detoxVerificationRepository.save(token);

        return phrase;
    }

    public boolean verifyPhraseAndEndDetox(Long memberId, String inputPhrase) {

        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        DetoxVerificationEntity token = detoxVerificationRepository
                .findByMember_IdAndUsedFalse(memberId)
                .orElse(null);

        if (token == null) {
            return false;
        }

        if (token.getExpiresAt() != null &&
                token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setUsed(true);
            detoxVerificationRepository.save(token);
            return false;
        }

        if (!token.getPhrase().equals(inputPhrase)) {
            return false;
        }

        LocalDate today = LocalDate.now(KST);
        LocalTime nowTime = LocalTime.now(KST);
        String todayDay = today.getDayOfWeek().name();

        List<TimeDetoxEntity> runningSchedules = repository.findAllByMember_Id(memberId)
                .stream()
                .filter(TimeDetoxEntity::isActive)
                .filter(schedule ->
                        schedule.getDay() != null &&
                                schedule.getDay().equalsIgnoreCase(todayDay)
                )
                .filter(schedule -> {
                    if ("BIWEEKLY".equalsIgnoreCase(schedule.getCycle())) {
                        return isCurrentWeekBiweekly(schedule.getCreatedDate());
                    }

                    return true;
                })
                .filter(schedule ->
                        !nowTime.isBefore(schedule.getStartTime()) &&
                                !nowTime.isAfter(schedule.getEndTime())
                )
                .filter(schedule ->
                        !isDisabledToday(memberId, schedule.getId(), today)
                )
                .toList();

        for (TimeDetoxEntity schedule : runningSchedules) {
            TimeDetoxDailyDisableEntity disableLog =
                    TimeDetoxDailyDisableEntity.create(member, schedule, today);

            dailyDisableRepository.save(disableLog);
        }

        token.setUsed(true);
        detoxVerificationRepository.save(token);

        return true;
    }

    public LockedAppDetails isAppLockedWithDetails(
            Long userId,
            String day,
            LocalTime currentTime
    ) {
        MemberEntity member = getMemberOrThrow(userId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        LocalDate today = LocalDate.now(KST);

        List<TimeDetoxEntity> activeSchedules = repository.findAllByMember_Id(userId)
                .stream()
                .filter(TimeDetoxEntity::isActive)
                .toList();

        List<String> lockedApps = new ArrayList<>();

        boolean isLocked = activeSchedules.stream().anyMatch(schedule -> {

            if (isDisabledToday(userId, schedule.getId(), today)) {
                return false;
            }

            if ("BIWEEKLY".equalsIgnoreCase(schedule.getCycle())) {
                if (!isCurrentWeekBiweekly(schedule.getCreatedDate())) {
                    return false;
                }
            }

            boolean inTimeRange =
                    !currentTime.isBefore(schedule.getStartTime()) &&
                            !currentTime.isAfter(schedule.getEndTime());

            if (schedule.getDay().equalsIgnoreCase(day) && inTimeRange) {
                lockedApps.addAll(schedule.getLockedApps());
                return true;
            }

            return false;
        });

        return new LockedAppDetails(isLocked, lockedApps);
    }

    private boolean isCurrentWeekBiweekly(LocalDate createdDate) {

        if (createdDate == null) {
            return true;
        }

        LocalDate today = LocalDate.now(KST);
        long weeksDifference = ChronoUnit.WEEKS.between(createdDate, today);

        return weeksDifference % 2 == 0;
    }

    public List<TimeDetoxDto> getAllTimeDetoxSchedulesByMember(Long memberId) {

        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        LocalDate today = LocalDate.now(KST);

        return repository.findAllByMember_Id(memberId)
                .stream()
                .map(entity -> {
                    TimeDetoxDto dto = convertToDTO(entity);

                    boolean disabledToday = isDisabledToday(
                            memberId,
                            entity.getId(),
                            today
                    );

                    dto.setDisabledToday(disabledToday);

                    return dto;
                })
                .toList();
    }

    public static class LockedAppDetails {

        private boolean isLocked;
        @Getter
        private List<String> lockedApps;

        public LockedAppDetails(boolean isLocked, List<String> lockedApps) {
            this.isLocked = isLocked;
            this.lockedApps = lockedApps;
        }

        public boolean isLocked() {
            return isLocked;
        }

    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with ID: " + userId)
                );
    }
}