package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.example.LifeMaster_BE.UserManager.User;
import com.example.LifeMaster_BE.UserManager.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SleepService {

    private final SleepRepository sleepRepository;
    private final UserRepository userRepository; // User 정보를 가져오기 위한 Repository

    public void makeSleep(SleepDto.Request request) {
        // 1. User 확인
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. Sleep 객체 생성
        Sleep sleep = Sleep.builder()
                .sleepDate(request.getSleepDate())
                .sleepStart(request.getSleepStart())
                .sleepEnd(request.getSleepEnd())
                .sleepMood(request.getSleepMood())
                .sleepAwakeCnt(request.getSleepAwakeCnt())
                .user(user)
                .build();

        // 3. 수면 점수 계산
        double sleepScore = calculateSleepScore(sleep);
        sleep.setSleepScore(sleepScore);

        // 4. DB 저장
        sleepRepository.save(sleep);
    }

    // 수면 점수 계산 메서드
    private double calculateSleepScore(Sleep sleep) {
        if (sleep.getSleepStart() == null || sleep.getSleepEnd() == null) {
            throw new IllegalArgumentException("수면 시작 시간과 종료 시간이 모두 필요합니다.");
        }

        // 수면 시간 계산 (시간 단위)
        long sleepDuration = java.time.Duration.between(sleep.getSleepStart(), sleep.getSleepEnd()).toHours();

        // 수면 시간 점수 계산 (7~9시간 기준)
        double durationScore;
        if (sleepDuration >= 7 && sleepDuration <= 9) {
            durationScore = 100;
        } else if (sleepDuration < 7) {
            durationScore = 100 * (sleepDuration / 7.0);
        } else {
            durationScore = 100 * (9.0 / sleepDuration);
        }

        // 기분 상태 가중치 적용
        double moodScore = switch (sleep.getSleepMood()) {
            case HAPPY -> 1.0;
            case SOSO -> 0.8;
            case BAD -> 0.5;
        };

        // 깬 횟수 페널티 계산
        int awakeCount = sleep.getSleepAwakeCnt() != null ? sleep.getSleepAwakeCnt() : 0;
        double awakePenalty = Math.max(1.0 - (0.1 * awakeCount), 0.5);

        // 최종 점수 계산
        double rawScore = durationScore * moodScore * awakePenalty;

        // 점수 보정 (0~100 범위)
        return Math.min(Math.max(rawScore, 0), 100);
    }

    // Sleep 수정 메서드
    public void updateSleep(SleepDto.Request request) {
        // 1. Sleep 엔티티 가져오기
        Sleep existingSleep = sleepRepository.findById(request.getSleepId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수면 데이터입니다."));

        // 2. User 확인 (수면 데이터를 사용자와 연결)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 3. Builder를 사용해 Sleep 업데이트
        Sleep updatedSleep = Sleep.builder()
                .sleepId(existingSleep.getSleepId()) // ID 유지
                .user(user) // 사용자 유지
                .sleepDate(request.getSleepDate() != null ? request.getSleepDate() : existingSleep.getSleepDate())
                .sleepStart(request.getSleepStart() != null ? request.getSleepStart() : existingSleep.getSleepStart())
                .sleepEnd(request.getSleepEnd() != null ? request.getSleepEnd() : existingSleep.getSleepEnd())
                .sleepMood(request.getSleepMood() != null ? request.getSleepMood() : existingSleep.getSleepMood())
                .sleepAwakeCnt(request.getSleepAwakeCnt() != null ? request.getSleepAwakeCnt() : existingSleep.getSleepAwakeCnt())
                .sleepScore(existingSleep.getSleepScore()) // 점수 초기화 (다시 계산)
                .build();

        // 4. 수면 점수 재계산
        double sleepScore = calculateSleepScore(updatedSleep);
        updatedSleep.setSleepScore(sleepScore);

        // 5. DB 저장
        sleepRepository.save(updatedSleep);
    }

    public List<SleepDto.Response> selectSleep(Integer userId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7); // 일주일 전 시간 계산

        // 사용자와 일주일 전 데이터 필터링
        List<Sleep> sleepList = sleepRepository.findByUserIdAndSleepDateAfter(userId, oneWeekAgo);

        // Sleep -> SleepDto.Response 변환
        return sleepList.stream()
                .map(sleep -> SleepDto.Response.builder()
                        .sleepId(sleep.getSleepId())
                        .sleepDate(sleep.getSleepDate())
                        .sleepStart(sleep.getSleepStart())
                        .sleepEnd(sleep.getSleepEnd())
                        .sleepMood(sleep.getSleepMood())
                        .sleepAwakeCnt(sleep.getSleepAwakeCnt())
                        .sleepScore(sleep.getSleepScore())
                        .build())
                .toList();
    }
}
