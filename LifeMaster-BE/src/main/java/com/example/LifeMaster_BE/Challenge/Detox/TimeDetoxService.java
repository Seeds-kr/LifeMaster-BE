package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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


    public void updateRandomPhrase() {
        this.currentRandomPhrase = randomPhraseProvider.getRandomPhrase();
    }

    public TimeDetoxDTO createSchedule(TimeDetoxDTO dto, Long memberId) {
        TimeDetoxEntity entity = new TimeDetoxEntity();
        entity.setCycle(dto.getCycle());
        entity.setDay(dto.getDay());
        entity.setStartTime(LocalTime.parse(dto.getStartTime()));
        entity.setEndTime(LocalTime.parse(dto.getEndTime()));
        entity.setActive(dto.isActive());
        entity.setLockedApps(dto.getLockedApps());
        entity.setMemberId(memberId); // 멤버 ID 설정

        TimeDetoxEntity savedEntity = repository.save(entity);

        // 저장된 엔티티를 DTO로 변환하여 반환
        return convertToDTO(savedEntity);
    }

    private TimeDetoxDTO convertToDTO(TimeDetoxEntity entity) {
        TimeDetoxDTO dto = new TimeDetoxDTO();
        dto.setId(entity.getId());
        dto.setCycle(entity.getCycle());
        dto.setDay(entity.getDay());
        dto.setStartTime(String.valueOf(entity.getStartTime()));
        dto.setEndTime(String.valueOf(entity.getEndTime()));
        dto.setActive(entity.isActive());
        dto.setLockedApps(entity.getLockedApps());
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

    public TimeDetoxEntity updateSchedule(Long id, TimeDetoxEntity updatedSchedule) {
        TimeDetoxEntity schedule = getScheduleById(id);
        schedule.setCycle(updatedSchedule.getCycle());
        schedule.setDay(updatedSchedule.getDay());
        schedule.setStartTime(updatedSchedule.getStartTime());
        schedule.setEndTime(updatedSchedule.getEndTime());
        schedule.setLockedApps(updatedSchedule.getLockedApps());
        return repository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        repository.deleteById(id);
    }

    // 특정 디톡스 활성화/비활성화 로직 수정
    public TimeDetoxEntity toggleActivation(Long id, String currentDay, LocalTime currentTime) {
        TimeDetoxEntity schedule = getScheduleById(id);

        if (!schedule.isActive()) {
            // 비활성화 상태일 경우 활성화
            schedule.setActive(true);
        } else {
            // 활성화 상태일 경우
            boolean inTimeRange = !currentTime.isBefore(schedule.getStartTime()) && !currentTime.isAfter(schedule.getEndTime());
            boolean isMatchingDay = schedule.getDay().equalsIgnoreCase(currentDay);

            if (isMatchingDay && inTimeRange) {
                // 현재 시간이 디톡스 시간 범위에 포함된 경우 비활성화 불가
                throw new IllegalStateException("현재 시간이 디톡스 활성화 시간 범위에 포함되어 있어 비활성화할 수 없습니다.");
            }

            // 현재 시간이 디톡스 시간 범위에 포함되지 않은 경우 비활성화
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
}
