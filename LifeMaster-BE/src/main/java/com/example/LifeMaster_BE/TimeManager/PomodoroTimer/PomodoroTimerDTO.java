package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PomodoroTimerDTO {
    private String taskName;
    private int CurrentTimer;  // 현재 시간 (분 단위)
    private int focusTime;  // 집중 시간 (분 단위)
    private int breakTime;  // 휴식 시간 (분 단위)
    private int cycles;     // 포모도로 반복 횟수
    private String date;    // 날짜 (예: "2024-11-14")
}
