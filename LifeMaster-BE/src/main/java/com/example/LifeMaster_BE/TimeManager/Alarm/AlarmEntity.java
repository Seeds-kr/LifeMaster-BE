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

    @Enumerated(EnumType.STRING)
    private RandomMissionType randomMissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_level")
    private MissionLevel missionLevel;

    public enum MissionLevel {
        HIGH, MEDIUM, LOW
    }

    // 수학 미션용 문제/정답 필드 추가
    private String mathQuestion;   // 예: "12 + 8"
    private Integer mathAnswer;    // 예: 20

    // 문장 따라쓰기 미션용 필드
    private String typingSentence; // 예: "The quick brown fox jumps over the lazy dog."

    // 따라 누르기 미션용 필드 (5x5 그리드를 JSON 문자열로 저장)
    @Lob
    private String followClickGridJson; // 예: "[[0,1,0,...],[...],...]"

    // DTO → Entity 변환
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

        if (dto.getRandomMissionType() != null) {
            alarm.randomMissionType = dto.getRandomMissionType();
        } else {
            alarm.randomMissionType = RandomMissionType.NONE;
        }

        alarm.missionLevel = dto.getMissionLevel();

        // 처음 생성 시에는 아직 문제/정답 없음 → null 로 시작
        alarm.mathQuestion = null;
        alarm.mathAnswer = null;
        alarm.typingSentence = null;
        alarm.followClickGridJson = null;

        return alarm;
    }
}