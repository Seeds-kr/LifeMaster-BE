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
    private final TimeDetoxRepository timeDetoxRepository;

    private final SubscriptionAccessService subscriptionAccessService;

    private final TimeDetoxDailyDisableRepository dailyDisableRepository;

    @Getter
    private String currentRandomPhrase;
    //@Autowired
    //private TimeDetoxRepository timeDetoxRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");


    public void updateRandomPhrase() {
        this.currentRandomPhrase = randomPhraseProvider.getRandomPhrase();
    }

    public TimeDetoxDto createSchedule(TimeDetoxDto dto, Long memberId) {

        MemberEntity member = getMemberOrThrow(memberId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        TimeDetoxEntity entity = new TimeDetoxEntity();
        entity.setCycle(dto.getCycle());
        entity.setDay(dto.getDay());

        // "10:30" / "18:30"만
        entity.setStartTime(LocalTime.parse(dto.getStartTime(), HH_MM));
        entity.setEndTime(LocalTime.parse(dto.getEndTime(), HH_MM));

        // lockedApps: String -> List<String>
        entity.setLockedApps(parseLockedApps(dto.getLockedApps()));

        // (선택) 내부적으로 기본 활성화 값이 필요하면 여기서만 세팅
        entity.setActive(true);

        entity.setMemberId(memberId);

        TimeDetoxEntity savedEntity = repository.save(entity);
        return convertToDTO(savedEntity);
    }

    private List<String> parseLockedApps(String lockedAppsRaw) {

        if (lockedAppsRaw == null || lockedAppsRaw.isBlank()) return Collections.emptyList();

        String s = lockedAppsRaw.trim();
        try {
            // JSON 배열 문자열이면: ["YouTube","Instagram"]
            if (s.startsWith("[")) {
                return new ObjectMapper().readValue(s, new TypeReference<List<String>>() {});
            }

            // 아니면 콤마 구분: YouTube,Instagram
            return List.of(s.split("\\s*,\\s*"));
        } catch (Exception e) {
            throw new IllegalArgumentException("lockedApps 형식이 올바르지 않습니다. JSON 배열 문자열 또는 콤마 구분 문자열을 사용하세요.", e);
        }
    }

    private TimeDetoxDto convertToDTO(TimeDetoxEntity entity) {
        TimeDetoxDto dto = new TimeDetoxDto();
        dto.setId(entity.getId());
        dto.setCycle(entity.getCycle());
        dto.setDay(entity.getDay());

        dto.setStartTime(entity.getStartTime() != null ? entity.getStartTime().format(HH_MM) : null);
        dto.setEndTime(entity.getEndTime() != null ? entity.getEndTime().format(HH_MM) : null);

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

    public List<TimeDetoxEntity> getAllSchedules(Long userId) {

        MemberEntity member = getMemberOrThrow(userId);

        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        return repository.findAllByMember_Id(userId);
    }

    public TimeDetoxEntity getScheduleById(Long id) {

        MemberEntity member = getMemberOrThrow(id);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        return repository.findById(id).orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public TimeDetoxEntity updateSchedule(Long memberId, Long id, TimeDetoxEntity updatedSchedule) {

        MemberEntity member = getMemberOrThrow(memberId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        // 내 스케줄만 조회 (소유권 체크)
        TimeDetoxEntity schedule = repository
                .findByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found or no permission"));

        schedule.setCycle(updatedSchedule.getCycle());
        schedule.setDay(updatedSchedule.getDay());
        schedule.setStartTime(updatedSchedule.getStartTime());
        schedule.setEndTime(updatedSchedule.getEndTime());
        schedule.setLockedApps(updatedSchedule.getLockedApps());

        return repository.save(schedule);
    }


    public void deleteSchedule(Long memberId, Long id) {

        MemberEntity member = getMemberOrThrow(memberId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        TimeDetoxEntity schedule = repository
                .findByIdAndMember_Id(id, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("해당 스케줄이 없거나 삭제 권한이 없습니다.")
                );

        repository.delete(schedule);
    }

    // 특정 디톡스 활성화/비활성화 로직 수정
    public TimeDetoxEntity toggleActivation(Long memberId, Long id, String currentDay, LocalTime currentTime) {

        MemberEntity member = getMemberOrThrow(memberId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        // 내 스케줄만 조회 (소유권 체크)
        TimeDetoxEntity schedule = repository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found or no permission"));

        if (!schedule.isActive()) {
            // 비활성화 상태일 경우 활성화
            schedule.setActive(true);
        } else {
            // 활성화 상태일 경우
            boolean inTimeRange =
                    !currentTime.isBefore(schedule.getStartTime()) &&
                            !currentTime.isAfter(schedule.getEndTime());

            boolean isMatchingDay = schedule.getDay() != null &&
                    schedule.getDay().equalsIgnoreCase(currentDay);

            // 요일이 같고, 시간이 범위 안이면 "비활성화 불가"
            if (isMatchingDay && inTimeRange) {
                throw new IllegalStateException("현재 시간이 디톡스 활성화 시간 범위에 포함되어 있어 비활성화할 수 없습니다.");
            }

            // 현재 시간이 범위 밖이면 비활성화
            schedule.setActive(false);
        }

        return repository.save(schedule);
    }

    // 유저별 랜덤 문구 생성
    public String generateRandomPhrase(Long memberId) {

        MemberEntity member = getMemberOrThrow(memberId);

        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        String phrase = randomPhraseProvider.getRandomPhrase();

        DetoxVerificationEntity token = detoxVerificationRepository
                .findByMember_IdAndUsedFalse(memberId)
                .orElseGet(() ->
                        DetoxVerificationEntity.create(member, phrase, null) // ✅ 기존 member 사용
                );

        token.setPhrase(phrase);
        token.setCreatedAt(LocalDateTime.now());
        token.setUsed(false);

        detoxVerificationRepository.save(token);

        return phrase;
    }

    // 디톡스 종료
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

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        String todayDay = today.getDayOfWeek().name();

        /*
         * 비상탈출은 디톡스 스케줄 자체를 끄는 것이 아니라,
         * 현재 실행 중인 "오늘의 시간잠금"만 비활성화 처리한다.
         */
        List<TimeDetoxEntity> runningSchedules = repository.findAllByMember_Id(memberId)
                .stream()
                .filter(TimeDetoxEntity::isActive)
                .filter(schedule -> schedule.getDay() != null &&
                        schedule.getDay().equalsIgnoreCase(todayDay))
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

    // 앱 잠금 여부 확인 및 잠긴 앱 목록 반환
    public LockedAppDetails isAppLockedWithDetails(Long userId, String day, LocalTime currentTime) {

        MemberEntity member = getMemberOrThrow(userId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        LocalDate today = LocalDate.now();

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

    // 격주 계산 로직
    private boolean isCurrentWeekBiweekly(LocalDate createdDate) {
        LocalDate today = LocalDate.now();
        long weeksDifference = ChronoUnit.WEEKS.between(createdDate, today);

        // 격주인지 여부를 계산 (주 차이가 짝수이면 격주 주기에 포함됨)
        return weeksDifference % 2 == 0;
    }
    /*
    @Transactional
    public void addAllowedApps(TimeDetoxDto.App request) {
        TimeDetoxEntity detox = timeDetoxRepository.findById(request.getDetoxId())
                .orElseThrow(() -> new RuntimeException("Detox not found"));

        List<String> currentApps = detox.getLockedApps();
        currentApps.addAll(request.getAllowedApps());
        detox.setLockedApps(currentApps);

        timeDetoxRepository.save(detox);
    }*/

    // 내부 클래스: 잠긴 상태와 앱 목록 반환 구조체
    public static class LockedAppDetails {
        private boolean isLocked;
        private List<String> lockedApps;

        public LockedAppDetails(boolean isLocked, List<String> lockedApps) {
            this.isLocked = isLocked;
            this.lockedApps = lockedApps;
        }

        public boolean isLocked() {
            return isLocked;
        }

        public List<String> getLockedApps() {
            return lockedApps;
        }
    }

    public List<TimeDetoxDto> getAllTimeDetoxSchedulesByMember(Long memberId) {

        MemberEntity member = getMemberOrThrow(memberId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.Detox);

        LocalDate today = LocalDate.now();

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

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}
