package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.TimeManager.Sleep.Sleep;
import com.example.LifeMaster_BE.TimeManager.Sleep.SleepRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SleepGoalProgressService {

    private final SleepRepository sleepRepository;

    /**
     * 특정 사용자의 기간 내 총 수면 시간을 "시간" 단위로 반환
     *
     * 예:
     * 7시간 30분 -> 7.5
     * 8시간      -> 8.0
     */
    @Transactional(readOnly = true)
    public double calculateSleepHours(
            MemberEntity user,
            LocalDate startDate,
            LocalDate endDate
    ) {

        List<Sleep> sleepList =
                sleepRepository.findByUserAndSleepDateBetween(
                        user,
                        startDate,
                        endDate
                );

        long totalMinutes = sleepList.stream()

                // 시작/종료 시간이 없는 잘못된 기록 제외
                .filter(sleep ->
                        sleep.getSleepStart() != null &&
                                sleep.getSleepEnd() != null
                )

                // 각각의 수면 시간을 분으로 계산
                .mapToLong(sleep ->
                        Duration.between(
                                sleep.getSleepStart(),
                                sleep.getSleepEnd()
                        ).toMinutes()
                )

                // 음수/0 데이터 제외
                .filter(minutes -> minutes > 0)

                // 전체 합산
                .sum();

        // 최종 결과는 "시간" 단위
        return totalMinutes / 60.0;
    }
}