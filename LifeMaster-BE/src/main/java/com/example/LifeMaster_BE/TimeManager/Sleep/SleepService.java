package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SleepService {

    private final SleepRepository sleepRepository;
    private final MemberRepository userRepository;

    // 수면 시작 기록 생성
    public void makeSleep(SleepDto.Request request) {
        MemberEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Sleep sleep = Sleep.builder()
                .sleepDate(request.getSleepDate())  // LocalDate만 저장
                .sleepStart(request.getSleepStart())
                .sleepEnd(request.getSleepEnd())
                .sleepMood(request.getSleepMood())
                .alarmSnoozeCnt(request.getAlarmSnoozeCnt())
                .timeToWakeUp(request.getTimeToWakeUp()) // 일어나는데 걸린 시간
                .antiSleepMode(request.getAntiSleepMode()) // 재수면 방지 여부
                .user(user)
                .build();

        double sleepScore = calculateSleepScore(sleep);
        sleep.setSleepScore(sleepScore);

        sleepRepository.save(sleep);
    }

    // 수면 점수 계산 로직
    private double calculateSleepScore(Sleep sleep) {
        if (sleep.getSleepStart() == null || sleep.getSleepEnd() == null) {
            throw new IllegalArgumentException("수면 시작/종료 시간이 필요합니다.");
        }

        // 1. 수면 시간 점수 (7~9시간 기준)
        long sleepDuration = Duration.between(sleep.getSleepStart(), sleep.getSleepEnd()).toHours();
        double durationScore;
        if (sleepDuration >= 7 && sleepDuration <= 9) {
            durationScore = 100;
        } else if (sleepDuration < 7) {
            durationScore = 100 * (sleepDuration / 7.0);
        } else {
            durationScore = 100 * (9.0 / sleepDuration);
        }

        // 2. 기분 상태 가중치
        double moodScore = switch (sleep.getSleepMood()) {
            case HAPPY -> 1.0;
            case SOSO -> 0.85;
            case BAD -> 0.6;
        };

        // 3. 알람 미루기 페널티
        int snoozeCnt = sleep.getAlarmSnoozeCnt() != null ? sleep.getAlarmSnoozeCnt() : 0;
        double snoozePenalty = Math.max(1.0 - (0.05 * snoozeCnt), 0.5);

        // 4. 일어나는데 걸린 시간 페널티 (10분 이상이면 점수 감소)
        int timeToWakeUp = sleep.getTimeToWakeUp() != null ? sleep.getTimeToWakeUp() : 0;
        double wakeUpPenalty = timeToWakeUp <= 10 ? 1.0 : Math.max(1.0 - (timeToWakeUp - 10) * 0.02, 0.6);

        // 5. 재수면 방지 모드 보너스
        double antiSleepBonus = (sleep.getAntiSleepMode() != null && sleep.getAntiSleepMode()) ? 1.05 : 1.0;

        // 최종 점수
        double rawScore = durationScore * moodScore * snoozePenalty * wakeUpPenalty * antiSleepBonus;

        return Math.min(Math.max(rawScore, 0), 100);
    }

    // 수면 데이터 수정
    public void updateSleep(SleepDto.Request request) {
        Sleep existingSleep = sleepRepository.findById(request.getSleepId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수면 데이터입니다."));

        MemberEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Sleep updatedSleep = Sleep.builder()
                .sleepId(existingSleep.getSleepId())
                .user(user)
                .sleepDate(request.getSleepDate() != null ? request.getSleepDate() : existingSleep.getSleepDate())
                .sleepStart(request.getSleepStart() != null ? request.getSleepStart() : existingSleep.getSleepStart())
                .sleepEnd(request.getSleepEnd() != null ? request.getSleepEnd() : existingSleep.getSleepEnd())
                .sleepMood(request.getSleepMood() != null ? request.getSleepMood() : existingSleep.getSleepMood())
                .alarmSnoozeCnt(request.getAlarmSnoozeCnt() != null ? request.getAlarmSnoozeCnt() : existingSleep.getAlarmSnoozeCnt())
                .timeToWakeUp(request.getTimeToWakeUp() != null ? request.getTimeToWakeUp() : existingSleep.getTimeToWakeUp())
                .antiSleepMode(request.getAntiSleepMode() != null ? request.getAntiSleepMode() : existingSleep.getAntiSleepMode())
                .build();

        double sleepScore = calculateSleepScore(updatedSleep);
        updatedSleep.setSleepScore(sleepScore);

        sleepRepository.save(updatedSleep);
    }

    // 수면 기록 조회
    public List<SleepDto.Response> selectSleep(Long userId) {
        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);

        MemberEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<Sleep> sleepList = sleepRepository.findByUserAndSleepDateAfter(user, oneWeekAgo);

        return sleepList.stream()
                .map(sleep -> SleepDto.Response.builder()
                        .sleepId(sleep.getSleepId())
                        .sleepDate(sleep.getSleepDate())
                        .sleepStart(sleep.getSleepStart())
                        .sleepEnd(sleep.getSleepEnd())
                        .sleepMood(sleep.getSleepMood())
                        .alarmSnoozeCnt(sleep.getAlarmSnoozeCnt())
                        .timeToWakeUp(sleep.getTimeToWakeUp())
                        .antiSleepMode(sleep.getAntiSleepMode())
                        .sleepScore(sleep.getSleepScore())
                        .build())
                .toList();
    }
}
