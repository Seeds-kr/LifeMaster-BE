package com.example.LifeMaster_BE.Admin.Report;

import com.example.LifeMaster_BE.Admin.Report.Dto.AdminReportDetailDto;
import com.example.LifeMaster_BE.Admin.Report.Dto.AdminReportListDto;
import com.example.LifeMaster_BE.Admin.Report.Dto.ReportActionRequest;
import com.example.LifeMaster_BE.Admin.Report.Dto.ReportStatusUpdateRequest;
import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import com.example.LifeMaster_BE.Report.ReportActionType;
import com.example.LifeMaster_BE.Report.ReportEntity;
import com.example.LifeMaster_BE.Report.ReportRepository;
import com.example.LifeMaster_BE.Report.ReportStatus;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public Page<AdminReportListDto> getReports(ReportStatus status, Pageable pageable) {
        Page<ReportEntity> reports;
        if (status != null) {
            reports = reportRepository.findByStatus(status, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        return reports.map(AdminReportListDto::from);
    }

    public AdminReportDetailDto getReportDetail(Long reportId) {
        ReportEntity report = reportRepository.findDetailById(reportId);
        if (report == null) {
            throw new IllegalArgumentException("신고를 찾을 수 없습니다. ID: " + reportId);
        }
        return AdminReportDetailDto.from(report);
    }

    @Transactional
    public void updateReportStatus(Long reportId, ReportStatusUpdateRequest request) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다. ID: " + reportId));
        report.updateStatus(request.getStatus());
    }

    @Transactional
    public void processReport(Long reportId, String adminEmail, ReportActionRequest request) {
        ReportEntity report = reportRepository.findDetailById(reportId);
        if (report == null) {
            throw new IllegalArgumentException("신고를 찾을 수 없습니다. ID: " + reportId);
        }

        MemberEntity admin = memberRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다. Email: " + adminEmail));

        System.out.println("=== 신고 처리 시작 ===");
        System.out.println("Report ID: " + reportId);
        System.out.println("Admin Email: " + adminEmail);
        System.out.println("Admin ID: " + admin.getId());
        System.out.println("Action Type: " + request.getActionType());

        // 신고 처리 (반려는 REJECTED 상태로, 나머지는 RESOLVED)
        ReportStatus targetStatus = (request.getActionType() == ReportActionType.REJECT)
                ? ReportStatus.REJECTED
                : ReportStatus.RESOLVED;
        report.resolve(admin, targetStatus, request.getActionType(), request.getAdminNote());

        System.out.println("resolve 호출 후:");
        System.out.println("Report Status: " + report.getStatus());
        System.out.println("ResolvedBy: " + (report.getResolvedBy() != null ? report.getResolvedBy().getId() : "null"));
        System.out.println("ResolvedBy Nickname: " + (report.getResolvedBy() != null ? report.getResolvedBy().getNickname() : "null"));

        // 조치 적용
        applyAction(report, request.getActionType());

        System.out.println("applyAction 호출 후:");
        System.out.println("Report Status: " + report.getStatus());
        System.out.println("ResolvedBy: " + (report.getResolvedBy() != null ? report.getResolvedBy().getId() : "null"));
        System.out.println("=== 신고 처리 완료 ===");
    }

    private void applyAction(ReportEntity report, ReportActionType actionType) {
        if (report.getPost() == null) {
            return;
        }

        MemberEntity postAuthor = report.getPost().getMember();
        if (postAuthor == null) {
            return;
        }

        switch (actionType) {
            case REJECT:
                // 반려는 별도 조치 없음
                break;
            case WARNING:
                postAuthor.setWarningCount(postAuthor.getWarningCount() + 1);
                postAuthor.setMemberStatus(MemberStatus.WARNED);
                break;
            case POST_DELETE:
                PostEntity postToDelete = report.getPost();
                // 게시글 삭제 전에 모든 신고에 게시글 정보 스냅샷 저장
                postToDelete.getReports().forEach(r -> {
                    r.savePostSnapshot(postToDelete);
                    r.setPost(null);  // 연관관계 해제
                });
                postRepository.delete(postToDelete);
                break;
            case SUSPEND_1D:
                postAuthor.suspend(1);
                break;
            case SUSPEND_7D:
                postAuthor.suspend(7);
                break;
            case SUSPEND_30D:
                postAuthor.suspend(30);
                break;
        }
    }
}
