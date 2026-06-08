package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PomodoroRecentFocusListResponseDto {

    private List<PomodoroRecentFocusItemDto> items;
}