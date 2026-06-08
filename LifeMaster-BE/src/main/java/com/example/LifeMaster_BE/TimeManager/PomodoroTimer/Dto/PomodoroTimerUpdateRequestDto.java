package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PomodoroTimerUpdateRequestDto {

    @Schema(description = "작업명", example = "오늘 첫번째 할일")
    private String taskName;

    @Schema(description = "1회 집중 시간, 분 단위", example = "25")
    private Integer focusTime;

    @Schema(description = "1회 휴식 시간, 분 단위", example = "5")
    private Integer breakTime;
}