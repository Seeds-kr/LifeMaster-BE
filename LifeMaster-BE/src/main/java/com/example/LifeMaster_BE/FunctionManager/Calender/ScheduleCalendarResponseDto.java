package com.example.LifeMaster_BE.FunctionManager.Calender;

import java.util.List;

public class ScheduleCalendarResponseDto {
    private Long id;
    private String date;
    private String day;
    private List<String> events;

    public ScheduleCalendarResponseDto(Long id, String date, String day, List<String> events) {
        this.id = id;
        this.date = date;
        this.day = day;
        this.events = events;
    }

    public Long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public List<String> getEvents() {
        return events;
    }
}
