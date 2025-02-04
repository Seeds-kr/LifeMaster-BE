package com.example.LifeMaster_BE.FunctionManager.Calender;


import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class ScheduleCalendarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String date; // 날짜

    private String day; // 요일

    @ElementCollection
    private List<String> events; // 이벤트 리스트

    // TodoEntity와의 관계 설정
    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("calendar") // 순환 참조 방지
    private List<TodoEntity> todos; // To-Do 리스트 (외래키 관계)

    // Getter와 Setter
}

