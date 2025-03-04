package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarEntity;
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
    @Column(name = "calendar_date", insertable = false, updatable = false)
    private String date;

    private String title;
    private boolean completed;

    @ManyToOne
    @JoinColumn(name = "calendar_date", referencedColumnName = "date", nullable = false, foreignKey = @ForeignKey(name = "FK_CALENDAR_ID"))
    @JsonIgnoreProperties("todos") // 순환 참조 방지
    private ScheduleCalendarEntity calendar;

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


