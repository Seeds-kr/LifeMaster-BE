package com.example.LifeMaster_BE.Admin.Report;

import com.example.LifeMaster_BE.Admin.Report.Dto.AdminReportDetailDto;
import com.example.LifeMaster_BE.Admin.Report.Dto.AdminReportListDto;
import com.example.LifeMaster_BE.Admin.Report.Dto.ReportActionRequest;
import com.example.LifeMaster_BE.Admin.Report.Dto.ReportStatusUpdateRequest;
import com.example.LifeMaster_BE.Report.ReportStatus;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Report", description = "관리자 신고 관리 API")
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(summary = "신고 목록 조회", description = "신고 목록을 페이징, 상태 필터로 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<AdminReportListDto>> getReports(
            @Parameter(description = "신고 상태 필터") @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminReportListDto> reports = adminReportService.getReports(status, pageable);
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "신고 상세 조회", description = "특정 신고의 상세 정보를 조회합니다.")
    @GetMapping("/{reportId}")
    public ResponseEntity<AdminReportDetailDto> getReportDetail(
            @Parameter(description = "신고 ID") @PathVariable Long reportId) {
        AdminReportDetailDto report = adminReportService.getReportDetail(reportId);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "신고 상태 변경", description = "신고의 상태를 변경합니다.")
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<Void> updateReportStatus(
            @Parameter(description = "신고 ID") @PathVariable Long reportId,
            @Valid @RequestBody ReportStatusUpdateRequest request) {
        adminReportService.updateReportStatus(reportId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "신고 처리", description = "신고를 처리합니다. (경고, 게시글 삭제, 정지 등)")
    @PostMapping("/{reportId}/action")
    public ResponseEntity<Void> processReport(
            @Parameter(description = "신고 ID") @PathVariable Long reportId,
            @Valid @RequestBody ReportActionRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();
        adminReportService.processReport(reportId, adminEmail, request);
        return ResponseEntity.ok().build();
    }
}
