package com.example.LifeMaster_BE.Admin.User;

import com.example.LifeMaster_BE.Admin.User.Dto.AdminUserDetailDto;
import com.example.LifeMaster_BE.Admin.User.Dto.AdminUserListDto;
import com.example.LifeMaster_BE.Admin.User.Dto.RoleUpdateRequest;
import com.example.LifeMaster_BE.UserManager.Member.MemberStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - User", description = "관리자 회원 관리 API")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "회원 목록 조회", description = "회원 목록을 페이징, 검색으로 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<AdminUserListDto>> getUsers(
            @Parameter(description = "검색 키워드 (이메일, 닉네임)") @RequestParam(required = false) String keyword,
            @Parameter(description = "회원 상태 필터") @RequestParam(required = false) MemberStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminUserListDto> users = adminUserService.getUsers(keyword, status, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "회원 상세 조회", description = "특정 회원의 상세 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailDto> getUserDetail(
            @Parameter(description = "회원 ID") @PathVariable Long userId) {
        AdminUserDetailDto user = adminUserService.getUserDetail(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "회원 역할 변경", description = "회원의 역할을 변경합니다. (USER, ADMIN)")
    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @Parameter(description = "회원 ID") @PathVariable Long userId,
            @Valid @RequestBody RoleUpdateRequest request) {
        adminUserService.updateUserRole(userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 삭제", description = "회원을 삭제합니다. (소프트 삭제)")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "회원 ID") @PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
