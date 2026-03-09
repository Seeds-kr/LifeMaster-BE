package com.example.LifeMaster_BE.Group.GoalAchievement;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups/{groupId}/achievements")
public class GoalAchievementController {

    private final GoalAchievementService goalAchievementService;

    @Operation(summary = "그룹 최근 30일 목표 달성 히트맵 조회")
    @GetMapping("/heatmap")
    public List<GoalAchievementHeatmapDto> getGroupHeatmap(
            @PathVariable("groupId") Long groupId
    ) {
        return goalAchievementService.getLast30DaysHeatmapByGroup(groupId);
    }

    @Operation(summary = "특정 목표 최근 30일 달성 히트맵 조회")
    @GetMapping("/goals/{goalId}/heatmap")
    public List<GoalAchievementHeatmapDto> getGoalHeatmap(
            @PathVariable("groupId") Long groupId,
            @PathVariable("goalId") Long goalId
    ) {
        return goalAchievementService.getLast30DaysHeatmapByGroupAndGoal(groupId, goalId);
    }
}