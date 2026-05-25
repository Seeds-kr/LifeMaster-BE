package com.example.LifeMaster_BE.Admin.Report.Dto;

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
public class AdminReportListDto {

    private Long id;
    private String reason;
    private ReportStatus status;
    private Long reporterId;
    private String reporterNickname;
    private Long postId;
    private String postTitle;
    private LocalDateTime createdAt;

    public static AdminReportListDto from(ReportEntity report) {
        return AdminReportListDto.builder()
                .id(report.getId())
                .reason(report.getReason())
                .status(report.getStatus())
                .reporterId(report.getMember() != null ? report.getMember().getId() : null)
                .reporterNickname(report.getMember() != null ? report.getMember().getNickname() : null)
                .postId(report.getPost() != null ? report.getPost().getId() : report.getPostIdSnapshot())
                .postTitle(report.getPost() != null ? report.getPost().getTitle() : report.getPostTitle())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
