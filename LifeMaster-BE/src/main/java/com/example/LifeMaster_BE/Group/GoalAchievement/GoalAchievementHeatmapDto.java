package com.example.LifeMaster_BE.Group.GoalAchievement;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class GoalAchievementHeatmapDto {
    private LocalDate date;
    private long achievedUserCount;
}