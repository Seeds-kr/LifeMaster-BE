package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyCountDto {
    private String month;
    private Long count;

    public MonthlyCountDto(Integer year, Integer month, Long count) {
        this.month = year + "-" + String.format("%02d", month);
        this.count = count;
    }
}
