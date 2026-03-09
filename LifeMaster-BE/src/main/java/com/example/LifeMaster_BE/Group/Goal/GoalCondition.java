package com.example.LifeMaster_BE.Group.Goal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "목표 기준")
public enum GoalCondition {
    @Schema(description = "시간 기준")
    TIME,

    @Schema(description = "횟수 기준")
    COUNT
}