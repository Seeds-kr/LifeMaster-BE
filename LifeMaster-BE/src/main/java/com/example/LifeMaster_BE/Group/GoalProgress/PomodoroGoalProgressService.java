package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.PomodoroTimerEntity;
import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.PomodoroTimerRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PomodoroGoalProgressService {

    private final PomodoroTimerRepository pomodoroTimerRepository;

    private static final DateTimeFormatter TIMER_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 특정 사용자의 기간 내 총 포모도로 집중시간을 반환
     *
     * 단위: 분
     *
     * 계산:
     * focusTime * completedCount
     */
    @Transactional(readOnly = true)
    public double calculatePomodoroMinutes(
            MemberEntity user,
            LocalDate startDate,
            LocalDate endDate
    ) {

        String start = startDate.format(TIMER_DATE_FORMATTER);
        String end = endDate.format(TIMER_DATE_FORMATTER);

        List<PomodoroTimerEntity> timers =
                pomodoroTimerRepository.findByMember_IdAndDateBetween(
                        user.getId(),
                        start,
                        end
                );

        return timers.stream()
                .mapToDouble(timer ->
                        timer.getFocusTime()
                                * timer.getCompletedCount()
                )
                .sum();
    }
}