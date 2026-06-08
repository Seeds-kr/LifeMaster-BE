package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.PomodoroFocusLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PomodoroFocusSaveRequestDto {

    @Schema(description = "저장할 날짜", example = "2026-06-08")
    private String date;

    @Schema(
            description = "집중도 레벨",
            example = "VERY_GOOD",
            allowableValues = {"LOW", "NORMAL", "GOOD", "VERY_GOOD"}
    )
    private PomodoroFocusLevel focusLevel;
}