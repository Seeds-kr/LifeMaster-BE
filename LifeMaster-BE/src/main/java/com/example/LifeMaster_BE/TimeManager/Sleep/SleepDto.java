package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

public class SleepDto {
    @Builder(toBuilder = true)
    @Getter
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Integer sleepId;
        private LocalDateTime sleepDate;     // 수면 날짜
        private LocalDateTime sleepStart;   // 수면 시작 시간
        private LocalDateTime sleepEnd;     // 수면 종료 시간
        private MoodStatus sleepMood;       // 수면 기분 상태 (HAPPY, SOSO, BAD)
        private Integer sleepAwakeCnt;      // 깬 횟수
        private Long userId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    @Getter
    public static class Response {
        private Integer sleepId;
        private LocalDateTime sleepDate;
        private LocalDateTime sleepStart;   // 수면 시작 시간
        private LocalDateTime sleepEnd;     // 수면 종료 시간
        private MoodStatus sleepMood;       // 수면 기분 상태 (HAPPY, SOSO, BAD)
        private Integer sleepAwakeCnt;      // 깬 횟수
        private Double sleepScore;
    }
}
