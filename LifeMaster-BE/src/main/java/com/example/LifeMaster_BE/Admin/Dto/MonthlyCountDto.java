package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyCountDto {
    private String month;
    private Long count;
}
