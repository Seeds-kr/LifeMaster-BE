package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPostSummaryDto {

    private long totalCount;

    private long freeCount;

    private long improvementCount;

    private long calendarSharedCount;
}