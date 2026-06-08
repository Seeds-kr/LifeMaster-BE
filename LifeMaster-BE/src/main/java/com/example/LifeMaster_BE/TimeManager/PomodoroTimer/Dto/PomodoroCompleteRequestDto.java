package com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PomodoroCompleteRequestDto {

    @Schema(description = "추가할 완료 횟수", example = "1")
    private int count = 1;
}