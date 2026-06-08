package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.PomodoroFocusLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PomodoroDailyFocusResponseDto {

    private String date;

    private int totalFocusMinutes;

    private PomodoroFocusLevel focusLevel;
}