package com.example.LifeMaster_BE.Challenge.Detox;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class TimeDetoxDto {
    @Builder(toBuilder = true)
    @Getter
    @Schema(name = "TimeDetoxApp")
    public static class App {
        private Long detoxId;
        private List<String> allowedApps;
    }
}
