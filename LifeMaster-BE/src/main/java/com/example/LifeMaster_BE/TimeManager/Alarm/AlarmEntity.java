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
    private boolean alarmStatus = true;

    private String alarmSound;

    private boolean snoozed;
    //더미
    @Column(name = "is_snoozed", nullable = false)
    private boolean legacySnoozed;

    private int snoozeTime;
    private Long snoozeCount;

    private boolean reSleptPrevention;
    private int reSleptPreventionTime;
    // === 기존 DB 컬럼 is_re_slept 호환용 더미 필드 ===
    @Column(name = "is_re_slept", nullable = false)
    private boolean legacyReSlept;  // 실제 로직에서는 안 쓰고, INSERT 시 값 채우기용

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private RandomMissionType randomMissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_level", nullable = true)
    private MissionLevel missionLevel;

    public enum MissionLevel {
        HIGH, MEDIUM, LOW
    }

    // 수학 미션용 문제/정답 필드 추가
    @JsonIgnore
    private String mathQuestion;   // 예: "12 + 8"

    @JsonIgnore
    private Integer mathAnswer;    // 예: 20

    // 문장 따라쓰기 미션용 필드
    @JsonIgnore
    private String typingSentence; // 예: "The quick brown fox jumps over the lazy dog."

    // 따라 누르기 미션용 필드 (5x5 그리드를 JSON 문자열로 저장)
    @Lob
    @JsonIgnore
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

        alarm.alarmStatus = true; // ⭐ 기본 활성화

        alarm.alarmSound = dto.getAlarmSound();
        alarm.snoozed = dto.isSnoozed();
        alarm.snoozeTime = dto.getSnoozeTime();
        alarm.snoozeCount = dto.getSnoozeCount();

        alarm.reSleptPrevention = dto.isReSleptPrevention();
        alarm.reSleptPreventionTime = dto.getReSleptPreventionTime();

        //더미
        alarm.legacyReSlept = alarm.reSleptPrevention;
        alarm.legacySnoozed = alarm.snoozed;

        alarm.randomMissionType = dto.getRandomMissionType();

        if (alarm.randomMissionType == null) {
            // 미션이 없으므로 레벨도 null 처리
            alarm.missionLevel = null;
        } else {
            // 미션이 있을 때만 레벨 세팅
            alarm.missionLevel = dto.getMissionLevel();
        }

        // 처음 생성 시에는 아직 문제/정답 없음 → null 로 시작
        alarm.mathQuestion = null;
        alarm.mathAnswer = null;
        alarm.typingSentence = null;
        alarm.followClickGridJson = null;

        return alarm;
    }
}