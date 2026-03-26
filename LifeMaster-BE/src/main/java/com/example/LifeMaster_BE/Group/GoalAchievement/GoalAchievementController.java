package com.example.LifeMaster_BE.Group.GoalAchievement;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups/{groupId}/achievements")
public class GoalAchievementController {

    private final GoalAchievementService goalAchievementService;
    private final Login login;

    @Operation(summary = "그룹 최근 30일 목표 달성 히트맵 조회")
    @GetMapping("/heatmap")
    public ResponseEntity<?> getGroupHeatmap(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("groupId") Long groupId
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        return ResponseEntity.ok(
                goalAchievementService.getLast30DaysHeatmapByGroup(user.getId(), groupId)
        );
    }

    @Operation(summary = "특정 목표 최근 30일 달성 히트맵 조회")
    @GetMapping("/goals/{goalId}/heatmap")
    public ResponseEntity<?> getGoalHeatmap(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("groupId") Long groupId,
            @PathVariable("goalId") Long goalId
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        return ResponseEntity.ok(
                goalAchievementService.getLast30DaysHeatmapByGroupAndGoal(user.getId(), groupId, goalId)
        );
    }
}