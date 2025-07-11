package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PomodoroTimerResponseDto {
    private Long id;
    private String taskName;
    private int focusTime;
    private int breakTime;
    private int currentTimer;
    private String date;
    private Long memberId;
}