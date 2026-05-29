package com.example.LifeMaster_BE.Challenge.Detox;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

public class RepeatDetoxDto {

    @Getter
    @Setter
    @Schema(name = "RepeatDetoxCreateRequest")
    public static class Request {

        private String lockedApp;
        private Integer sessionUsageLimit;
        private Integer lockDuration;
        private Integer dailyMaxUsageLimit;
    }

    @Getter
    @Builder
    @Schema(name = "RepeatDetoxResponse")
    public static class Response {

        private Long id;
        private String lockedApp;
        private Integer sessionUsageLimit;
        private Integer lockDuration;
        private Integer dailyMaxUsageLimit;

        // 추가
        private Integer todayUsedMinutes;
    }

    @Getter
    @Builder
    @Schema(name = "RepeatDetoxListResponse")
    public static class ListResponse {

        private List<Response> lockedApps;
    }

    @Getter
    @Builder
    @Schema(name = "RepeatDetoxLockStatusResponse")
    public static class LockStatusResponse {

        private Long id;
        private String lockedApp;
        private boolean locked;
    }

    @Getter
    @Builder
    @Schema(name = "RepeatDetoxDetailResponse")
    public static class DetailResponse {

        private Long id;
        private String lockedApp;

        private Integer todayUsedMinutes;
        private Integer remainingUnlockMinutes;
        private boolean exceededDailyLimit;
    }

    @Getter
    @Builder
    @Schema(name = "RepeatDetoxPhraseResponse")
    public static class PhraseResponse {

        private String phrase;
    }
}