package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DailyCountDto {
    private LocalDate date;
    private Long count;

    public DailyCountDto(LocalDateTime dateTime, Long count) {
        this.date = dateTime.toLocalDate();
        this.count = count;
    }
}
