package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.PomodoroFocusLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PomodoroFocusSaveRequestDto {

    private String date; // yyyy-MM-dd

    private int totalFocusMinutes;

    private PomodoroFocusLevel focusLevel;
}