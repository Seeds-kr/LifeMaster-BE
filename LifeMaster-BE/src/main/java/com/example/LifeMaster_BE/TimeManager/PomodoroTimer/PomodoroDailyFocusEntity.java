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

    // yyyy-MM-dd
    @Column(nullable = false)
    private String date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PomodoroFocusLevel focusLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;
}