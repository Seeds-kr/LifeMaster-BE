package com.example.LifeMaster_BE.TimeManager.Alarm.Dto;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum.RandomMissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NewAlarmDto {

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

    private boolean snoozed;
    private LocalDateTime snoozeTime;
    private Long snoozeCount;

    private boolean reSlept;
    private LocalDateTime reSleptTime;

    // ==== 랜덤 미션 ====
    private RandomMissionType randomMissionType;

    private AlarmEntity.MissionLevel missionLevel;
}



/*
time, title,  mon~sun,
sound 와 미루기 (유무, 몇분, 횟수 포함),
다시 잠들기 방지(유무, 몇분),
랜덤미션 유형(수학 같은 경우 난이도도 포함) 필요할 것 같아요.
 */