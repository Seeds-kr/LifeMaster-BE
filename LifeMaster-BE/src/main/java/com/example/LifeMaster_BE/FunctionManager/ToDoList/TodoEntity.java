package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class TodoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Getter와 Setter
    private String date;

    private String title;
    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false, foreignKey = @ForeignKey(name = "FK_CALENDAR_ID"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ScheduleCalendarEntity calendar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;

    // 기본 생성자
    public TodoEntity() {
    }

    public void setCalendar(ScheduleCalendarEntity calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("Calendar cannot be null");
        }
        this.calendar = calendar;
    }
}


