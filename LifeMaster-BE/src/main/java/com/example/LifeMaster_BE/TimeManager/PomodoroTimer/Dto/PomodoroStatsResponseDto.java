package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.PomodoroFocusLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PomodoroStatsResponseDto {

    private String date;

    // 오늘 총 집중 시간
    private int todayTotalFocusMinutes;

    // 오늘 집중 시간이 평소보다 얼마나 많은지
    private int focusMinutesDiff;

    // 완료한 뽀모도로 횟수
    private int completedCount;

    // 완료 횟수가 평소보다 얼마나 많은지
    private int completedCountDiff;

    // 오늘 평균 집중 시간
    private int averageFocusMinutes;

    // 평균 집중 시간이 평소보다 얼마나 많은지
    private int averageFocusMinutesDiff;

    // 주간 누적 집중 시간
    private int weeklyTotalFocusMinutes;

    // 오늘의 집중도
    private PomodoroFocusLevel focusLevel;
}