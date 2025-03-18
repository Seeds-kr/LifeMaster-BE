package com.example.LifeMaster_BE.Challenge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class ChallengeDto {

    @Builder(toBuilder = true)
    @Getter
    @Schema(name = "ChallengeCreate")
    public static class Create {
    private String challName;
    private String challDesc;
    private String challImg;
    }

    @Builder(toBuilder = true)
    @Getter
    @Schema(name = "ChallengeList")
    public static class List {
        private Integer challId;
        private String challName;
        private String challDesc;
        private String challImg;
        private Boolean challMe;// 본인의 참여 여부 조회
        private Integer challCnt;
    }


}
