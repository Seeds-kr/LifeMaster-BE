package com.example.LifeMaster_BE.TimeManager.Alarm.Snooze;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;

@Entity
@Getter
public class SnoozeEntity {

    @Id
    @GeneratedValue
    private Long id;

    // Alarm
    @OneToOne
    @JoinColumn(name = "alarm_id")
    private AlarmEntity alarm;

    // 총 반복 횟수
    private Long snoozeSum;
    // 현재 반복 횟수
    private Long snoozePre;
    // 마지막 시간
    private Timestamp snoozeLast;
    // 시간 간격
    private Timestamp snoozeInt;
}
