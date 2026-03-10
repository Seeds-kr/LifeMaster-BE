package com.example.LifeMaster_BE.Group.GoalProgress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "목표 진행 기록 응답")
public class GoalProgressResponseDTO {

    @Schema(description = "진행 기록 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "7")
    private Long userId;

    @Schema(description = "사용자 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "그룹 ID", example = "3")
    private Long groupId;

    @Schema(description = "목표 ID", example = "5")
    private Long goalId;

    @Schema(description = "목표 이름", example = "수면 7시간")
    private String goalName;

    @Schema(description = "진행 값", example = "2")
    private int progressValue;

    @Schema(description = "제출 시간")
    private LocalDateTime submittedAt;
}