package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PomodoroStatsResponseDto {

    private String date;

    // 오늘 총 누적 집중 시간
    private int todayTotalFocusMinutes;

    // 오늘 누적 집중 시간 - 직전 30일의 하루 누적 집중 시간 평균
    private int focusMinutesDiff;

    // 오늘 완료한 포모도로 횟수
    private int completedCount;

    // 오늘 완료 횟수 - 직전 30일 하루 완료 횟수 평균
    private int completedCountDiff;

    // 오늘 평균 집중 시간
    private int averageFocusMinutes;

    // 오늘 평균 집중 시간 - 직전 30일 하루 평균 집중 시간 평균
    private int averageFocusMinutesDiff;

    // 오늘 포함 최근 7일 누적 집중 시간
    private int weeklyTotalFocusMinutes;
}