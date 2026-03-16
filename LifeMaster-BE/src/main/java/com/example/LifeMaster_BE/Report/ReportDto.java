package com.example.LifeMaster_BE.Report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportDto {

    @NotNull
    private Long postId;

    @NotBlank
    private String reason;
}
