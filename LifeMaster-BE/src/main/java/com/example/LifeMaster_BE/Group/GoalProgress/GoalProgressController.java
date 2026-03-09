package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/group/goal-progress")
public class GoalProgressController {
    private final GoalProgressService goalProgressService;
    
    private final GoalProgressRepository goalProgressRepository;
    private final GoalRepository goalRepository;

    private final MemberRepository memberRepository;

    public GoalProgressController(GoalProgressService goalProgressService, GoalProgressRepository goalProgressRepository, GoalRepository goalRepository, MemberRepository memberRepository) {
        this.goalProgressService = goalProgressService;
        this.goalProgressRepository = goalProgressRepository;
        this.goalRepository = goalRepository;
        this.memberRepository = memberRepository;
    }

    @Operation(summary = "목표별 진행률", description = "목표별 진행률")
    @GetMapping("/{goalId}")
    public double getGoalProgress(@PathVariable("goalId") Long goalId) {
        Optional<GoalEntity> goal = goalRepository.findById(goalId);
        if (goal.isEmpty()) {
            throw new IllegalArgumentException("Goal not found");
        }
        return goalProgressService.calculateProgress(goal.get());
    }

    @Operation(summary = "사용자별 목표 진행률", description = "사용자별 목표 진행률")
    @GetMapping("/{goalId}/user")
    public double getUserGoalProgress(
            @PathVariable("goalId") Long goalId,
            @RequestParam("userEmail") String userEmail
    ) {
        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with id: " + goalId));

        MemberEntity user = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        return goalProgressService.calculateUserProgress(goal, user);
    }

    @Operation(summary = "목표 수행", description = "사용자가 해당하는 목표(공부하기/푸쉬업하기 등)에 특정 값(시간/횟수)를 추가합니다")
    @PostMapping("/add")
    public ResponseEntity<GoalProgressEntity> addGoalProgress(
            @RequestParam("groupId") Long groupId,
            @RequestParam("goalId") Long goalId,
            @RequestParam("userEmail") String userEmail,
            @RequestParam("progressValue") int progressValue) {

        GoalProgressEntity goalProgress = goalProgressService.addGoalProgress(groupId, goalId, userEmail, progressValue);
        return ResponseEntity.ok(goalProgress);
    }

    /**
     * 목표 진행 기록 삭제
     */
    @Operation(summary = "목표 진행 기록 삭제", description = "목표 진행 기록 삭제")
    @DeleteMapping("/delete/{progressId}")
    public ResponseEntity<String> deleteGoalProgress(@PathVariable("progressId") Long progressId) {
        goalProgressService.deleteGoalProgress(progressId);
        return ResponseEntity.ok("Goal progress deleted successfully.");
    }

    /**
     * 전체 목표 진행 기록 조회
     */
    @Operation(summary = "전체 목표 진행 기록 조회", description = "전체 목표 진행 기록 조회")
    @GetMapping("/all")
    public ResponseEntity<List<GoalProgressEntity>> getAllGoalProgress() {
        List<GoalProgressEntity> progressList = goalProgressService.getAllGoalProgress();
        return ResponseEntity.ok(progressList);
    }

    /**
     * 사용자 이메일별 목표 진행 기록 조회
     */
    @Operation(summary = "사용자 이메일별 목표 진행 기록 조회", description = "사용자 이메일별 목표 진행 기록 조회")
    @GetMapping("/user")
    public ResponseEntity<List<GoalProgressEntity>> getGoalProgressByUserEmail(@RequestParam("userEmail") String userEmail) {
        List<GoalProgressEntity> progressList = goalProgressService.getGoalProgressByUserEmail(userEmail);
        return ResponseEntity.ok(progressList);
    }
}

