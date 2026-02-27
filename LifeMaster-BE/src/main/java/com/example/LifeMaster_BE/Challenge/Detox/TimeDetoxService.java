package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
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

    @Getter
    private String currentRandomPhrase;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");


    public void updateRandomPhrase() {
        this.currentRandomPhrase = randomPhraseProvider.getRandomPhrase();
    }

    public TimeDetoxDTO createSchedule(TimeDetoxDTO dto, Long memberId) {

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

    private TimeDetoxDTO convertToDTO(TimeDetoxEntity entity) {
        TimeDetoxDTO dto = new TimeDetoxDTO();
        dto.setId(entity.getId());
        dto.setCycle(entity.getCycle());
        dto.setDay(entity.getDay());

        // HH:mm 로만 내려주기
        dto.setStartTime(entity.getStartTime() != null ? entity.getStartTime().format(HH_MM) : null);
        dto.setEndTime(entity.getEndTime() != null ? entity.getEndTime().format(HH_MM) : null);

        // List<String> -> String(JSON)
        try {
            dto.setLockedApps(objectMapper.writeValueAsString(entity.getLockedApps()));
        } catch (Exception e) {
            dto.setLockedApps("[]");
        }

        return dto;
    }

    public List<TimeDetoxEntity> getAllSchedules(String email) {
        MemberEntity user = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return repository.findAllByMember(user);
    }

    public TimeDetoxEntity getScheduleById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public TimeDetoxEntity updateSchedule(Long memberId, Long id, TimeDetoxEntity updatedSchedule) {

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

        TimeDetoxEntity schedule = repository
                .findByIdAndMember_Id(id, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("해당 스케줄이 없거나 삭제 권한이 없습니다.")
                );

        repository.delete(schedule);
    }

    // 특정 디톡스 활성화/비활성화 로직 수정
    public TimeDetoxEntity toggleActivation(Long memberId, Long id, String currentDay, LocalTime currentTime) {

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
        String phrase = randomPhraseProvider.getRandomPhrase();

        // 기존 토큰 있으면 재사용 대신 덮어쓰기
        DetoxVerificationEntity token = detoxVerificationRepository
                .findByMember_IdAndUsedFalse(memberId)
                .orElseGet(() -> {
                    MemberEntity member = new MemberEntity();
                    member.setId(memberId);
                    return DetoxVerificationEntity.create(member, phrase, null);
                });

        token.setPhrase(phrase);
        token.setCreatedAt(LocalDateTime.now());
        token.setUsed(false);
        // 필요하면 expiresAt도 여기서 설정
        detoxVerificationRepository.save(token);

        return phrase;
    }

    // 디톡스 종료
    public boolean verifyPhraseAndEndDetox(Long memberId, String inputPhrase) {
        // 1) 토큰 조회
        DetoxVerificationEntity token = detoxVerificationRepository
                .findByMember_IdAndUsedFalse(memberId)
                .orElse(null);

        if (token == null) {
            return false;
        }

        // 2) (선택) 만료 시간 체크
        if (token.getExpiresAt() != null &&
                token.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 만료된 토큰이면 실패 처리
            token.setUsed(true);
            detoxVerificationRepository.save(token);
            return false;
        }

        // 3) 문구 비교
        if (!token.getPhrase().equals(inputPhrase)) {
            return false;
        }

        // 4) 문구 OK → 이 유저의 활성 디톡스 스케줄 종료
        List<TimeDetoxEntity> activeSchedules =
                timeDetoxRepository.findByMember_IdAndIsActiveTrue(memberId);

        activeSchedules.forEach(s -> s.setActive(false));
        timeDetoxRepository.saveAll(activeSchedules);

        // 5) 토큰 사용 처리 (또는 delete)
        token.setUsed(true);
        detoxVerificationRepository.save(token);

        return true;
    }

    // 앱 잠금 여부 확인 및 잠긴 앱 목록 반환
    public LockedAppDetails isAppLockedWithDetails(String day, LocalTime currentTime) {
        List<TimeDetoxEntity> activeSchedules = repository.findByIsActiveTrue();
        List<String> lockedApps = new ArrayList<>();

        boolean isLocked = activeSchedules.stream().anyMatch(schedule -> {
            // 격주 여부 확인
            if (schedule.getCycle().equalsIgnoreCase("BIWEEKLY")) {
                if (!isCurrentWeekBiweekly(schedule.getCreatedDate())) {
                    return false; // 이번 주가 해당 스케줄의 격주 주기가 아니면 스킵
                }
            }

            // 시간 범위 확인
            boolean inTimeRange = !currentTime.isBefore(schedule.getStartTime()) && !currentTime.isAfter(schedule.getEndTime());
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

    public List<TimeDetoxDTO> getAllTimeDetoxSchedulesByMember(Long memberId) {
        return repository.findAllByMember_Id(memberId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
}
