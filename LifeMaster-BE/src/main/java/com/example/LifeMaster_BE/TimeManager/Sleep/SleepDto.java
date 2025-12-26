package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        private LocalDate sleepDate;          // 수면 날짜 (우선 사용)
        private LocalDateTime sleepStart;     // fallback 용
        private LocalDateTime sleepEnd;
        private MoodStatus sleepMood;
        private AlarmInfoDto alarmInfo;

        public String getDate() {
            if (sleepDate != null) {
                return sleepDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }

            if (sleepStart != null) {
                return sleepStart
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }

            throw new IllegalStateException("Sleep date 정보가 없습니다 (sleepDate, sleepStart 모두 null)");
        }
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

