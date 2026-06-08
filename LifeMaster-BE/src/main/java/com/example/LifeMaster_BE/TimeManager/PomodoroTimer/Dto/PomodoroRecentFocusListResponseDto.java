package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto.PomodoroDailyFocusResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PomodoroRecentFocusListResponseDto {

    private List<PomodoroDailyFocusResponseDto> items;
}