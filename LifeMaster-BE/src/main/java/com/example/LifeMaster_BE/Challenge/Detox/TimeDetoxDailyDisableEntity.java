package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "time_detox_daily_disable",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TIME_DETOX_DAILY_DISABLE",
                        columnNames = {"member_id", "time_detox_id", "disabled_date"}
                )
        }
)
public class TimeDetoxDailyDisableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저가 비상탈출했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 어떤 시간잠금 스케줄을 오늘만 비활성화했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_detox_id", nullable = false)
    private TimeDetoxEntity timeDetox;

    // 비활성화 날짜
    @Column(name = "disabled_date", nullable = false)
    private LocalDate disabledDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static TimeDetoxDailyDisableEntity create(
            MemberEntity member,
            TimeDetoxEntity timeDetox,
            LocalDate disabledDate
    ) {
        TimeDetoxDailyDisableEntity entity = new TimeDetoxDailyDisableEntity();
        entity.setMember(member);
        entity.setTimeDetox(timeDetox);
        entity.setDisabledDate(disabledDate);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}