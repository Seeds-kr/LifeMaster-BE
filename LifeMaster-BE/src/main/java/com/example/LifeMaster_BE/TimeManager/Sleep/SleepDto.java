package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SleepDto {

    @Builder(toBuilder = true)
    @Getter
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Integer sleepId;
        private LocalDate sleepDate;        // 수면 날짜 (년-월-일)
        private LocalDateTime sleepStart;   // 수면 시작 시간
        private LocalDateTime sleepEnd;     // 수면 종료 시간
        private MoodStatus sleepMood;       // 수면 기분 상태 (VERY_BAD, BAD, GOOD, VERY_GOOD)
        private Integer alarmSnoozeCnt;     // 알람 미루기 횟수
        private Integer timeToWakeUp;       // 일어나는데 걸린 시간 (분)
        private Boolean antiSleepMode;      // 재수면 방지 여부
        private Long userId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    @Getter
    public static class Response {
        private Integer sleepId;
        private LocalDate sleepDate;
        private LocalDateTime sleepStart;   // 수면 시작 시간
        private LocalDateTime sleepEnd;     // 수면 종료 시간
        private MoodStatus sleepMood;       // 수면 기분 상태
        private Integer alarmSnoozeCnt;     // 알람 미루기 횟수
        private Integer timeToWakeUp;       // 일어나는데 걸린 시간 (분)
        private Boolean antiSleepMode;      // 재수면 방지 여부
        private Double sleepScore;          // 계산된 점수
    }
}
