package com.example.LifeMaster_BE.Challenge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

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

        public List(Challenge challenge, boolean isMe) {
            this.challId = Math.toIntExact(challenge.getChallId());
            this.challName = challenge.getChallName();
            this.challDesc = challenge.getChallDesc();
            this.challImg = challenge.getChallImg();
            this.challMe = isMe;
            this.challCnt = challenge.getChallCnt();
        }
    }
}
