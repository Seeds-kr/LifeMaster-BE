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
@Schema(name = "GoalCreateForm", description = "목표 생성 폼")
public class GoalDTO {

    @NotBlank(message = "목표 이름은 필수입니다.")
    @Schema(
            description = "목표 이름",
            example = "수면 7시간",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @NotNull(message = "목표 기준은 필수입니다.")
    @Schema(
            description = "목표 기준",
            allowableValues = {"TIME", "COUNT"},
            example = "TIME",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private GoalCondition goalCondition;

    @NotNull(message = "목표값은 필수입니다.")
    @Min(value = 1, message = "목표값은 1 이상이어야 합니다.")
    @Schema(
            description = "목표값",
            example = "7",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer value;

    @NotNull(message = "목표 기한은 필수입니다.")
    @Schema(
            description = "목표 기한",
            allowableValues = {"DAILY", "WEEKLY", "MONTHLY"},
            example = "DAILY",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private GoalDuration duration;
}