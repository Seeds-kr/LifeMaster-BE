package com.example.LifeMaster_BE.FunctionManager.Calender;

import java.util.List;

public class ScheduleCalendarRequestDto {
    private String date;
    private List<String> events;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }
}

