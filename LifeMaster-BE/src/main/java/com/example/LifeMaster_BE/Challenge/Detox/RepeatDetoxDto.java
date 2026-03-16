package com.example.LifeMaster_BE.Challenge.Detox;

import lombok.*;
import java.util.List;

public class RepeatDetoxDto {

    @Getter
    @Setter
    public static class Request {

        private String lockedApp;

        private Integer sessionUsageLimit;

        private Integer lockDuration;

        private Integer dailyMaxUsageLimit;
    }

    @Getter
    @Builder
    public static class Response {

        private Long id;

        private String lockedApp;

        private Integer sessionUsageLimit;

        private Integer lockDuration;

        private Integer dailyMaxUsageLimit;
    }

    @Getter
    @Builder
    public static class ListResponse {

        private List<Response> lockedApps;
    }
}