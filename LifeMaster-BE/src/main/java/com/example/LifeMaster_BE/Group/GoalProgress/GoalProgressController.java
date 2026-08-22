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

    @Operation(summary = "목표별 진행률", description = "해당 목표에 대한 그룹 전체 진행률을 반환합니다.")
    @GetMapping("/{goalId}")
    public ResponseEntity<?> getGoalProgress(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long goalId) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with id: " + goalId));

        return ResponseEntity.ok(goalProgressService.calculateProgress(user.getId(), goal));
    }

    @Operation(summary = "사용자별 목표 진행률", description = "현재 로그인한 사용자의 목표 진행률을 반환합니다.")
    @GetMapping("/{goalId}/user")
    public ResponseEntity<?> getUserGoalProgress(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long goalId) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with id: " + goalId));

        MemberEntity member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getId()));

        return ResponseEntity.ok(goalProgressService.calculateUserProgress(goal, member));
    }
}