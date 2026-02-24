package com.example.LifeMaster_BE.Admin;

import com.example.LifeMaster_BE.Admin.Dto.DashboardSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Dashboard", description = "관리자 대시보드 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "대시보드 요약 조회", description = "회원수, 게시글수, 미처리 신고수 등 대시보드 요약 정보를 조회합니다.")
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        DashboardSummaryDto summary = adminService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
}
