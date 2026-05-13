package com.example.LifeMaster_BE.Admin;

import com.example.LifeMaster_BE.Admin.Dto.DashboardSummaryDto;
import com.example.LifeMaster_BE.Admin.Dto.UserStatisticsResponse;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Dashboard", description = "관리자 대시보드 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final MemberRepository memberRepository;

    @Operation(summary = "대시보드 요약 조회", description = "회원수, 게시글수, 미처리 신고수 등 대시보드 요약 정보를 조회합니다.")
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        DashboardSummaryDto summary = adminService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * 2. 전체 회원 활동 통계 조회
     */
    @Operation(
            summary = "전체 회원 활동 통계 조회",
            description = "모든 회원의 챌린지, 그룹, 게시글, 댓글, Todo 등 활동 통계를 조회합니다."
    )
    @GetMapping("/user-statistics")
    public ResponseEntity<List<UserStatisticsResponse>> getUserStatistics(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);

        return ResponseEntity.ok(adminService.getUserStatistics());
    }

    /**
     * 3. 특정 회원 활동 통계 조회 (POST 방식)
     */
    @Operation(
            summary = "특정 회원 활동 통계 조회",
            description = "회원 userId를 전달받아 해당 회원의 활동 통계를 조회합니다."
    )
    @PostMapping("/user-statistics/{userId}")
    public ResponseEntity<UserStatisticsResponse> getUserStatisticsDetail(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);

        return ResponseEntity.ok(
                adminService.getUserStatisticsDetail(userId)
        );
    }

    private void validateAdmin(CustomUserDetails user) {
        MemberEntity member = memberRepository.findById(user.getId()).orElseThrow();
        if(member.getLoginRole() != LoginRole.ADMIN) {
            throw new RuntimeException("관리자만 접근 가능합니다.");
        }
    }
}
