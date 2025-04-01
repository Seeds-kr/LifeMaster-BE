package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class PomodoroTimerEntity {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskName;
    private int CurrentTimer;  // 현재 시간 (분 단위)
    private int focusTime;  // 집중 시간 (분 단위)
    private int breakTime;  // 휴식 시간 (분 단위)
    private int cycles;     // 포모도로 반복 횟수
    private String date;    // 날짜 (예: "2024-11-14")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;

}

