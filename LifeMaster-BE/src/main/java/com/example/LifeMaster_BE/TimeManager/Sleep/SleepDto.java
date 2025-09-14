package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SleepDto {

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlarmSettingsDto {
        private Integer alarmSnoozeCnt;
        private Integer timeToWakeUp;
        private Boolean antiSleepMode;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlarmInfoDto {
        private Boolean isWakeUpAlarmSet;
        private AlarmSettingsDto alarmSettings;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Integer sleepId;
        private LocalDate sleepDate;
        private LocalDateTime sleepStart;
        private LocalDateTime sleepEnd;
        private MoodStatus sleepMood;
        private AlarmInfoDto alarmInfo; // ✅ 묶어서 받음
        private Long userId;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private Integer sleepId;
        private LocalDate sleepDate;
        private LocalDateTime sleepStart;
        private LocalDateTime sleepEnd;
        private MoodStatus sleepMood;
        private AlarmInfoDto alarmInfo; // ✅ 묶어서 내려줌
        private Double sleepScore;
    }
}

