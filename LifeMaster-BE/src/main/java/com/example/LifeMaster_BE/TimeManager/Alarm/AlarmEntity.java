package com.example.LifeMaster_BE.TimeManager.Alarm;

import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.NewAlarmDto;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum.RandomMissionType;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
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

    private boolean snoozed;
    private LocalDateTime snoozeTime;
    private Long snoozeCount;

    private boolean reSlept;
    private LocalDateTime reSleptTime;

    // ✅ String → Enum 변경
    @Enumerated(EnumType.STRING)
    private RandomMissionType randomMissionType;

    // ✅ DTO → Entity 변환
    public static AlarmEntity fromDto(NewAlarmDto dto) {
        AlarmEntity alarm = new AlarmEntity();

        alarm.alarmTitle = dto.getAlarmTitle();
        alarm.alarmTime = dto.getAlarmTime();

        alarm.alarmMon = dto.isAlarmMon();
        alarm.alarmTue = dto.isAlarmTue();
        alarm.alarmWed = dto.isAlarmWed();
        alarm.alarmThu = dto.isAlarmThu();
        alarm.alarmFri = dto.isAlarmFri();
        alarm.alarmSat = dto.isAlarmSat();
        alarm.alarmSun = dto.isAlarmSun();

        alarm.alarmSound = dto.getAlarmSound();
        alarm.snoozed = dto.isSnoozed();
        alarm.snoozeTime = dto.getSnoozeTime();
        alarm.snoozeCount = dto.getSnoozeCount();

        alarm.reSlept = dto.isReSlept();
        alarm.reSleptTime = dto.getReSleptTime();

        // ✅ 문자열로 들어온 경우 Enum 변환 처리
        if (dto.getRandomMissionType() != null) {
            alarm.randomMissionType = RandomMissionType.fromString(dto.getRandomMissionType().name());
        } else {
            alarm.randomMissionType = RandomMissionType.NONE;
        }

        return alarm;
    }
}