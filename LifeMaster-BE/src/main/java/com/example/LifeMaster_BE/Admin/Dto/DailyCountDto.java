package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyCountDto {
    private String date;
    private Long count;
}
