package com.example.LifeMaster_BE.FunctionManager.Calender;


import jakarta.persistence.*;
import java.util.List;

@Entity
public class CalendarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date; // 날짜

    private String day; // 요일

    @ElementCollection
    private List<String> events; // 이벤트 리스트

    // Getter와 Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getday() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }
}

