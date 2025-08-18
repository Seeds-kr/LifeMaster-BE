package com.example.LifeMaster_BE.TimeManager.Alarm.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAlarmDto {

    private String alarmTitle;
    private LocalDateTime alarmTime;

    private boolean alarmMon;
    private boolean alarmTue;
    private boolean alarmWed;
    private boolean alarmThu;
    private boolean alarmFri;
    private boolean alarmSat;
    private boolean alarmSun;

    private String alarmSound;

    private boolean isSnoozed;
    private LocalDateTime snoozeTime;
    private Long snoozeCount;

    private boolean isReSlept;
    private LocalDateTime reSleptTime;

    private String randomMissionType;
}
