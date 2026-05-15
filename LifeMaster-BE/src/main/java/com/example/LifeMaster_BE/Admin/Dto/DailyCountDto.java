package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class DailyCountDto {

    private String date;
    private Long count;

    public DailyCountDto(Object date, Long count) {
        this.date = String.valueOf(date);
        this.count = count;
    }
}
