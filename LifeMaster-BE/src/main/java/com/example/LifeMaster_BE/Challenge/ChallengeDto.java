package com.example.LifeMaster_BE.Challenge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ChallengeDto {

    @Builder(toBuilder = true)
    @Getter
    @Schema(name = "ChallengeCreate")
    public static class Create {

        private String challName;
        private String challDesc;
        private String challImg;

        // 챌린지 시작 날짜
        private LocalDate challDate;
        
        public String getDate() {
            if (challDate == null) {
                throw new IllegalStateException("challDate가 null입니다.");
            }
            DateTimeFormatter DateTimeFormatter = null;
            return challDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
    }

    @Builder(toBuilder = true)
    @Getter
    @Schema(name = "ChallengeList")
    public static class List {
        private Integer challId;
        private String challName;
        private String challDesc;
        private String challImg;
        private Boolean challMe;
        private Integer challCnt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ChallengeCompleteRequest")
    public static class CompleteRequest {
        private Long challId;
    }

    @Builder
    @Getter
    @Schema(name = "ChallengeCompleteResponse")
    public static class CompleteResponse {
        private Long challId;
        private String completedAt;
    }

    @Builder
    @Getter
    @Schema(name = "ChallengeCompleteStatusResponse")
    public static class CompleteStatusResponse {

        private Long challId;

        // 오늘 완료 여부
        private Boolean completed;

        // 완료 시간 (HH:mm)
        private String completedAt;
    }
}
