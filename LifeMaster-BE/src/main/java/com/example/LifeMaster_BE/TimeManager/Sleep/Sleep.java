package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sleep")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class Sleep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sleepId;

    @Column(nullable = false)
    private LocalDate sleepDate;  // 수면 날짜 (년-월-일만 저장)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MemberEntity user;

    @Column(nullable = false)
    private LocalDateTime sleepStart;

    private LocalDateTime sleepEnd;

    private LocalDateTime sleepAlarm; // 필요하다면 유지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoodStatus sleepMood;

    private Integer alarmSnoozeCnt; // 알람 미루기 횟수

    private Integer timeToWakeUp;   // 일어나는데 걸린 시간 (분)

    private Boolean antiSleepMode;  // 재수면 방지 여부

    private Double sleepScore;

    public void setSleepScore(double sleepScore) {
        this.sleepScore = sleepScore;
    }

    public Duration getSleepDuration() {
        if (sleepStart != null && sleepEnd != null) {
            return Duration.between(sleepStart, sleepEnd);
        }
        return Duration.ZERO;
    }
}
