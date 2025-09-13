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

    // ✅ 알람 여부만 Sleep에 직접 저장
    private Boolean isWakeUpAlarmSet;

    // ✅ 알람 상세 정보는 Embeddable 객체로 묶음
    @Embedded
    private AlarmSettings alarmSettings;

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
