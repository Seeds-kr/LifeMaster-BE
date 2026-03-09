package com.example.LifeMaster_BE.Group.Goal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "목표 생성 요청 DTO")
public class GoalDTO {

    @NotBlank
    @Schema(description = "목표 이름", example = "수면 7시간")
    private String name;

    @NotNull
    @Schema(
            description = "목표 기준",
            implementation = GoalCondition.class,
            example = "TIME"
    )
    private GoalCondition goalCondition;

    @NotNull
    @Min(1)
    @Schema(description = "목표값", example = "7")
    private Integer value;

    @NotNull
    @Schema(
            description = "목표 기한",
            implementation = GoalDuration.class,
            example = "DAILY"
    )
    private GoalDuration duration;

    @Schema(description = "그룹 ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long groupId;
}