package com.example.LifeMaster_BE.TimeManager.Alarm;

import com.example.LifeMaster_BE.TimeManager.Alarm.Snooze.SnoozeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // Update operations for alarmStatus
public class AlarmEntity {

    @Id
    @GeneratedValue
    private Long id;

    // 회원 연결
    // alarm - mission

    // snooze
    @OneToOne(mappedBy = "alarm")
    private SnoozeEntity snooze;

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

    private String alarmSound;
    private Long alarmAnti;

    @Column(nullable = false)
    private boolean alarmStatus;


}
