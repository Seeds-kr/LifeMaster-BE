package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group/goal-progress")
public class GoalProgressController {

    private final GoalProgressService goalProgressService;
    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;
    private final Login login;

    public GoalProgressController(GoalProgressService goalProgressService, GoalRepository goalRepository, MemberRepository memberRepository, Login login) {
        this.goalProgressService = goalProgressService;
        this.goalRepository = goalRepository;
        this.memberRepository = memberRepository;
        this.login = login;
    }

    @Operation(summary = "목표별 진행률", description = "목표별 진행률")
    @GetMapping("/{goalId}")
    public ResponseEntity<?> getGoalProgress(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("goalId") Long goalId
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with id: " + goalId));

        double progress = goalProgressService.calculateProgress(user.getId(), goal);
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "사용자별 목표 진행률", description = "현재 로그인한 사용자 기준 목표 진행률")
    @GetMapping("/{goalId}/user")
    public ResponseEntity<?> getUserGoalProgress(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("goalId") Long goalId
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with id: " + goalId));

        MemberEntity member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getId()));

        double progress = goalProgressService.calculateUserProgress(goal, member);
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "목표 수행", description = "현재 로그인한 사용자가 해당 목표에 특정 값(시간/횟수)을 추가합니다")
    @PostMapping("/add")
    public ResponseEntity<?> addGoalProgress(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("groupId") Long groupId,
            @RequestParam("goalId") Long goalId,
            @RequestParam("progressValue") int progressValue
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        GoalProgressEntity goalProgress = goalProgressService.addGoalProgress(
                groupId,
                goalId,
                user.getId(),
                progressValue
        );

        return ResponseEntity.ok(goalProgress);
    }

    @Operation(summary = "목표 진행 기록 삭제", description = "현재 로그인한 사용자의 목표 진행 기록 삭제")
    @DeleteMapping("/delete/{progressId}")
    public ResponseEntity<?> deleteGoalProgress(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("progressId") Long progressId
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        goalProgressService.deleteGoalProgress(user.getId(), progressId);
        return ResponseEntity.ok("Goal progress deleted successfully.");
    }

    @Operation(summary = "전체 목표 진행 기록 조회", description = "전체 목표 진행 기록 조회")
    @GetMapping("/all")
    public ResponseEntity<?> getAllGoalProgress(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        List<GoalProgressResponseDTO> progressList = goalProgressService.getAllGoalProgress();
        return ResponseEntity.ok(progressList);
    }

    @Operation(summary = "사용자별 목표 진행 기록 조회", description = "현재 로그인한 사용자의 목표 진행 기록 조회")
    @GetMapping("/user")
    public ResponseEntity<?> getGoalProgressByUserId(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        List<GoalProgressResponseDTO> progressList =
                goalProgressService.getGoalProgressByUserId(user.getId());

        return ResponseEntity.ok(progressList);
    }
}