package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group/goal-progress")
public class GoalProgressController {

    private final GoalProgressService goalProgressService;
    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;

    public GoalProgressController(
            GoalProgressService goalProgressService,
            GoalRepository goalRepository,
            MemberRepository memberRepository
    ) {
        this.goalProgressService = goalProgressService;
        this.goalRepository = goalRepository;
        this.memberRepository = memberRepository;
    }

    @Operation(summary = "목표별 진행률", description = "목표별 진행률")
    @GetMapping("/{goalId}")
    public double getGoalProgress(@PathVariable("goalId") Long goalId) {
        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with id: " + goalId));

        return goalProgressService.calculateProgress(goal);
    }

    @Operation(summary = "사용자별 목표 진행률", description = "사용자 ID 기준 목표 진행률")
    @GetMapping("/{goalId}/user")
    public double getUserGoalProgress(
            @PathVariable("goalId") Long goalId,
            @RequestParam("userId") Long userId
    ) {
        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with id: " + goalId));

        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return goalProgressService.calculateUserProgress(goal, user);
    }

    @Operation(summary = "목표 수행", description = "사용자가 해당 목표에 특정 값(시간/횟수)을 추가합니다")
    @PostMapping("/add")
    public ResponseEntity<GoalProgressEntity> addGoalProgress(
            @RequestParam("groupId") Long groupId,
            @RequestParam("goalId") Long goalId,
            @RequestParam("userId") Long userId,
            @RequestParam("progressValue") int progressValue
    ) {
        GoalProgressEntity goalProgress = goalProgressService.addGoalProgress(groupId, goalId, userId, progressValue);
        return ResponseEntity.ok(goalProgress);
    }

    @Operation(summary = "목표 진행 기록 삭제", description = "목표 진행 기록 삭제")
    @DeleteMapping("/delete/{progressId}")
    public ResponseEntity<String> deleteGoalProgress(@PathVariable("progressId") Long progressId) {
        goalProgressService.deleteGoalProgress(progressId);
        return ResponseEntity.ok("Goal progress deleted successfully.");
    }

    @Operation(summary = "전체 목표 진행 기록 조회", description = "전체 목표 진행 기록 조회")
    @GetMapping("/all")
    public ResponseEntity<List<GoalProgressEntity>> getAllGoalProgress() {
        List<GoalProgressEntity> progressList = goalProgressService.getAllGoalProgress();
        return ResponseEntity.ok(progressList);
    }

    @Operation(summary = "사용자 ID별 목표 진행 기록 조회", description = "사용자 ID별 목표 진행 기록 조회")
    @GetMapping("/user")
    public ResponseEntity<List<GoalProgressEntity>> getGoalProgressByUserId(
            @RequestParam("userId") Long userId
    ) {
        List<GoalProgressEntity> progressList = goalProgressService.getGoalProgressByUserId(userId);
        return ResponseEntity.ok(progressList);
    }
}