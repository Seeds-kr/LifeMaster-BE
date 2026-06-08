package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포모도로 집중도 레벨")
public enum PomodoroFocusLevel {

    @Schema(description = "낮음")
    LOW,

    @Schema(description = "보통")
    NORMAL,

    @Schema(description = "좋음")
    GOOD,

    @Schema(description = "매우 좋음")
    VERY_GOOD
}