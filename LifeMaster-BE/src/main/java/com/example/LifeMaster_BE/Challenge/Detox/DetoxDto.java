package com.example.LifeMaster_BE.Challenge.Detox;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class DetoxDto {

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @Schema(name = "DetoxRequest")
    public static class Request {
        private Long detoxLock; // 잠금 시간
        private Long detoxUse;  // 사용 시간
        private Long detoxBreak; // 최대 사용 시간 (Break Time)
    }
}
