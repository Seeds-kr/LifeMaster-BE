package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class PomodoroTimerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskName;

    // 현재 타이머 값
    private int currentTimer;

    // 1회 집중 시간, 분 단위
    private int focusTime;

    // 1회 휴식 시간, 분 단위
    private int breakTime;

    // 날짜 저장 형식: yyyyMMdd
    private String date;

    // 완료한 포모도로 반복 횟수
    @Column(nullable = false)
    private int completedCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TodoEntity todo;
}