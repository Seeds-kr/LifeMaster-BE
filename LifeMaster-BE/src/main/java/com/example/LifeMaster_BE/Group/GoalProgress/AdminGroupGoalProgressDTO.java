package com.example.LifeMaster_BE.Group.GoalProgress;

import java.time.LocalDateTime;

public record AdminGroupGoalProgressDTO(
        Long progressId,
        Long userId,
        String userEmail,
        String userNickname,
        Long goalId,
        String goalName,
        String goalType,
        String goalDuration,
        double progressValue,
        LocalDateTime submittedAt
) {
}