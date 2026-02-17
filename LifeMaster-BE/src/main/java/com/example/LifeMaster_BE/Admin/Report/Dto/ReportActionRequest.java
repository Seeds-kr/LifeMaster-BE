package com.example.LifeMaster_BE.Admin.Report.Dto;

import com.example.LifeMaster_BE.Report.ReportActionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportActionRequest {

    @NotNull(message = "조치 유형은 필수입니다.")
    private ReportActionType actionType;

    private String adminNote;
}
