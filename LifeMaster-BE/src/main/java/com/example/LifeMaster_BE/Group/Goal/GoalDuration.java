package com.example.LifeMaster_BE.Group.Goal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "목표 기한")
public enum GoalDuration {
    @Schema(description = "일간")
    DAILY,

    @Schema(description = "주간")
    WEEKLY,

    @Schema(description = "월간")
    MONTHLY
}