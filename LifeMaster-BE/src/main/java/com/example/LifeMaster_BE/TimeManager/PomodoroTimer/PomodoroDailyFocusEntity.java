package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "pomodoro_daily_focus",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_POMODORO_DAILY_FOCUS_MEMBER_DATE",
                        columnNames = {"member_id", "date"}
                )
        }
)
public class PomodoroDailyFocusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // API 기준 날짜: yyyy-MM-dd
    @Column(nullable = false)
    private String date;

    // 해당 날짜의 총 누적 집중 시간
    @Column(nullable = false)
    private int totalFocusMinutes;

    // 해당 날짜의 완료한 포모도로 횟수
    @Column(nullable = false)
    private int completedCount;

    // 해당 날짜의 평균 집중 시간
    // 계산식: totalFocusMinutes / completedCount
    @Column(nullable = false)
    private int averageFocusMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PomodoroFocusLevel focusLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;
}