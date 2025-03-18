package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "sleep")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Sleep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sleepId;  // 기본 키

    @Column(nullable = false)
    private LocalDateTime sleepDate;  // 수면 날짜

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MemberEntity user;  // 사용자, User 엔티티와의 관계

    @Column(nullable = false)
    private LocalDateTime sleepStart;  // 수면 시작 시간

    private LocalDateTime sleepEnd;  // 수면 종료 시간 (NULL 가능)

    private LocalDateTime sleepAlarm; // 알람이 울린 시간 (NULL 가능)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoodStatus sleepMood;  // 오늘의 기분 (ENUM 타입)

    @Column
    private Integer sleepAwakeCnt; // 꺤 횟수

    @Column(nullable = true)
    private Double sleepScore;  // 알람 점수

    public void setSleepScore(double sleepScore) {
        this.sleepScore = sleepScore;
    }

    public Duration getSleepDuration() {
        LocalDateTime sleepstart = this.getSleepStart();
        LocalDateTime sleepend = this.getSleepEnd();
        if (sleepstart != null && sleepend != null) {
            return Duration.between(sleepstart, sleepend);
        }
        return Duration.ZERO;
    }
}
