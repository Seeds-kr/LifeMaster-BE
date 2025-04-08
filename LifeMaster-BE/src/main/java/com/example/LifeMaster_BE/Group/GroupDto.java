package com.example.LifeMaster_BE.Group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class GroupDto {

    @Builder(toBuilder = true)
    @Getter
    @Schema(name = "GroupStatic")
    public static class Static {
        private List<Long> userSleepDurations; // 유저의 하루 수면 시간 (분)
        private List<Long> groupAverageSleepDurations; // 그룹 평균 하루 수면 시간 (분)
    }
}
