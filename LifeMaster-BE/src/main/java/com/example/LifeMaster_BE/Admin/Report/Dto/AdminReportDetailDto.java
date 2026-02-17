package com.example.LifeMaster_BE.Admin.Report.Dto;

import com.example.LifeMaster_BE.Report.ReportActionType;
import com.example.LifeMaster_BE.Report.ReportEntity;
import com.example.LifeMaster_BE.Report.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportDetailDto {

    private Long id;
    private String reason;
    private ReportStatus status;
    private ReportActionType actionType;
    private String adminNote;

    // 신고자 정보
    private Long reporterId;
    private String reporterNickname;
    private String reporterEmail;

    // 게시글 정보
    private Long postId;
    private String postTitle;
    private String postContent;
    private Long postAuthorId;
    private String postAuthorNickname;

    // 처리 정보
    private Long resolvedById;
    private String resolvedByNickname;
    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;

    public static AdminReportDetailDto from(ReportEntity report) {
        return AdminReportDetailDto.builder()
                .id(report.getId())
                .reason(report.getReason())
                .status(report.getStatus())
                .actionType(report.getActionType())
                .adminNote(report.getAdminNote())
                // 신고자 정보
                .reporterId(report.getMember() != null ? report.getMember().getId() : null)
                .reporterNickname(report.getMember() != null ? report.getMember().getNickname() : null)
                .reporterEmail(report.getMember() != null ? report.getMember().getEmail() : null)
                // 게시글 정보
                .postId(report.getPost() != null ? report.getPost().getId() : null)
                .postTitle(report.getPost() != null ? report.getPost().getTitle() : null)
                .postContent(report.getPost() != null ? report.getPost().getContent() : null)
                .postAuthorId(report.getPost() != null && report.getPost().getMember() != null
                        ? report.getPost().getMember().getId() : null)
                .postAuthorNickname(report.getPost() != null && report.getPost().getMember() != null
                        ? report.getPost().getMember().getNickname() : null)
                // 처리 정보
                .resolvedById(report.getResolvedBy() != null ? report.getResolvedBy().getId() : null)
                .resolvedByNickname(report.getResolvedBy() != null ? report.getResolvedBy().getNickname() : null)
                .resolvedAt(report.getResolvedAt())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
