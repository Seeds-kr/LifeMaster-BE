package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/group/goal-progress")
public class GoalProgressController {
    private final GoalProgressService goalProgressService;
    private final GoalRepository goalRepository;

    public GoalProgressController(GoalProgressService goalProgressService, GoalRepository goalRepository) {
        this.goalProgressService = goalProgressService;
        this.goalRepository = goalRepository;
    }

    @GetMapping("/{goalId}")
    public double getGoalProgress(@PathVariable("goalId") Long goalId) {
        Optional<GoalEntity> goal = goalRepository.findById(goalId);
        if (goal.isEmpty()) {
            throw new IllegalArgumentException("Goal not found");
        }
        return goalProgressService.calculateProgress(goal.get());
    }

    @GetMapping("/{goalId}/user")
    public double getUserGoalProgress(@PathVariable("goalId") Long goalId, @RequestParam("userEmail") String userEmail) {
        Optional<GoalEntity> goal = goalRepository.findById(goalId);
        if (goal.isEmpty()) {
            throw new IllegalArgumentException("Goal not found");
        }
        return goalProgressService.calculateUserProgress(goal.get(), userEmail);
    }

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
    @DeleteMapping("/delete/{progressId}")
    public ResponseEntity<String> deleteGoalProgress(@PathVariable("progressId") Long progressId) {
        goalProgressService.deleteGoalProgress(progressId);
        return ResponseEntity.ok("Goal progress deleted successfully.");
    }

    /**
     * 전체 목표 진행 기록 조회
     */
    @GetMapping("/all")
    public ResponseEntity<List<GoalProgressEntity>> getAllGoalProgress() {
        List<GoalProgressEntity> progressList = goalProgressService.getAllGoalProgress();
        return ResponseEntity.ok(progressList);
    }

    /**
     * 사용자 이메일별 목표 진행 기록 조회
     */
    @GetMapping("/user")
    public ResponseEntity<List<GoalProgressEntity>> getGoalProgressByUserEmail(@RequestParam("userEmail") String userEmail) {
        List<GoalProgressEntity> progressList = goalProgressService.getGoalProgressByUserEmail(userEmail);
        return ResponseEntity.ok(progressList);
    }
}

