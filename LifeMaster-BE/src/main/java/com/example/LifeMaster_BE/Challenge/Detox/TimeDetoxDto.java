package com.example.LifeMaster_BE.Challenge.Detox;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TimeDetoxDto {
    private Long id;
    private String cycle;
    private String day;

    @Schema(example = "10:30")
    private String startTime;

    @Schema(example = "18:30")
    private String endTime;

    @Schema(
            description = "잠금 앱 목록 문자열(JSON 문자열 또는 콤마 구분)",
            example = "[\"YouTube\",\"Instagram\",\"Facebook\"]"
    )
    private String lockedApps;

    // ❌ active 제거 (요구사항)
}
