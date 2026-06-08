package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.PomodoroFocusLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PomodoroDailyFocusResponseDto {

    @Schema(description = "날짜", example = "2026-06-08")
    private String date;

    @Schema(description = "해당 날짜의 총 집중 시간", example = "180")
    private int totalFocusMinutes;

    @Schema(
            description = "집중도 레벨",
            example = "VERY_GOOD",
            allowableValues = {"LOW", "NORMAL", "GOOD", "VERY_GOOD"}
    )
    private PomodoroFocusLevel focusLevel;
}