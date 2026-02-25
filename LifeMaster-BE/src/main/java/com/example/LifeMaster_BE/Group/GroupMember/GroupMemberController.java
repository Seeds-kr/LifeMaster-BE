package com.example.LifeMaster_BE.Group.GroupMember;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/group")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;
    private final Login login;

    // 그룹 멤버(권한 포함) 목록 조회
    @Operation(summary = "그룹 멤버 권한 목록", description = "그룹에 속한 멤버들의 role(OWNER/ADMIN/MEMBER) 목록을 반환합니다.")
    @GetMapping("/{groupId}/members/roles")
    public ResponseEntity<?> getMembersWithRoles(
            @Parameter(description = "그룹 ID") @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long requestUserId = user.getId();
        List<GroupMemberEntity> members = groupMemberService.getMembersByGroup(groupId, requestUserId);
        return ResponseEntity.ok(members);
    }

    // 멤버 등급 변경 (OWNER/ADMIN 가능, 규칙은 서비스에 있음)
    @Operation(summary = "그룹 멤버 등급 변경", description = "특정 멤버의 등급을 변경합니다. (서비스 규칙 적용)")
    @PatchMapping("/{groupId}/members/{targetUserId}/role")
    public ResponseEntity<?> changeMemberRole(
            @Parameter(description = "그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "대상 유저 ID") @PathVariable Long targetUserId,
            @RequestParam("role") GroupMemberRole role,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long requestUserId = user.getId();
        GroupMemberEntity updated = groupMemberService.changeRole(groupId, requestUserId, targetUserId, role);
        return ResponseEntity.ok(updated);
    }

    // 소유권 이전 (OWNER만 가능)
    @Operation(summary = "그룹 소유권 이전", description = "OWNER가 다른 멤버에게 OWNER 권한을 이전합니다.")
    @PostMapping("/{groupId}/members/{newOwnerUserId}/transfer-ownership")
    public ResponseEntity<?> transferOwnership(
            @Parameter(description = "그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "새 OWNER 유저 ID") @PathVariable Long newOwnerUserId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long requestUserId = user.getId();
        groupMemberService.transferOwnership(groupId, requestUserId, newOwnerUserId);
        return ResponseEntity.ok("Ownership transferred successfully.");
    }

    // 내 그룹에서 내 role 조회
    @Operation(summary = "내 그룹 등급 조회", description = "특정 그룹에서 현재 로그인 유저의 role을 반환합니다.")
    @GetMapping("/{groupId}/me/role")
    public ResponseEntity<?> getMyRoleInGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        GroupMemberRole role = groupMemberService.getRole(groupId, user.getId());
        return ResponseEntity.ok(role);
    }
}
