package com.example.LifeMaster_BE.Admin.Report.Dto;

import com.example.LifeMaster_BE.Report.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    private ReportStatus status;
}
