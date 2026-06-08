package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PomodoroTimerDTO {
    private String taskName;
    private Long todoId;
    private int focusTime;    // 집중 시간 (분 단위)
    private int breakTime;    // 휴식 시간 (분 단위)
    private int currentTimer; // 현재 타이머 값
    private String date;      // 타이머가 속한 날짜 (YYYYMMDD)
}