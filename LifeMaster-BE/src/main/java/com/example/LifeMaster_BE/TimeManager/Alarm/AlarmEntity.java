package com.example.LifeMaster_BE.TimeManager.Alarm;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class AlarmEntity {

    @Id
    @GeneratedValue
    private Long id;

    // 회원 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;


    private LocalDateTime alarmTime;
    private String alarmTitle;

    @Column(nullable = false)
    private boolean alarmMon;
    @Column(nullable = false)
    private boolean alarmTue;
    @Column(nullable = false)
    private boolean alarmWed;
    @Column(nullable = false)
    private boolean alarmThu;
    @Column(nullable = false)
    private boolean alarmFri;
    @Column(nullable = false)
    private boolean alarmSat;
    @Column(nullable = false)
    private boolean alarmSun;

    @Column(nullable = false)
    private boolean alarmStatus;

    private String alarmSound;

    private boolean isSnoozed;
    private LocalDateTime snoozeTime;
    private Long snoozeCount;

    private boolean isReSlept;
    private LocalDateTime reSleptTime;

    private String randomMissionType;
}
