package com.example.LifeMaster_BE.Admin.Group;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/groups")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGroupController {

    private final AdminGroupService adminGroupService;
    private final GoalProgressService goalProgressService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getGroupDashboard(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(adminGroupService.getGroupDashboard());
    }

    @GetMapping
    public ResponseEntity<?> getAllGroups(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(adminGroupService.getAllGroups());
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(adminGroupService.getGroupMembers(groupId));
    }

    @GetMapping("/{groupId}/activity")
    public ResponseEntity<?> getGroupActivity(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(adminGroupService.getGroupActivity(groupId));
    }

    @DeleteMapping("/{groupId}/abnormal")
    public ResponseEntity<?> deleteAbnormalGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        adminGroupService.deleteAbnormalGroup(groupId);
        return ResponseEntity.ok("비정상 그룹이 삭제되었습니다.");
    }

    @DeleteMapping("/{groupId}/force")
    public ResponseEntity<?> forceDeleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        adminGroupService.forceDeleteGroup(groupId);
        return ResponseEntity.ok("관리자 권한으로 그룹이 삭제되었습니다.");
    }

    @GetMapping("/{groupId}/goal-progress")
    public ResponseEntity<?> getGroupGoalProgress(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        Sort.Order.desc("submittedAt"),
                        Sort.Order.desc("id")
                )
        );

        return ResponseEntity.ok(
                goalProgressService.getGoalProgressByGroupId(groupId, pageable)
        );
    }
}