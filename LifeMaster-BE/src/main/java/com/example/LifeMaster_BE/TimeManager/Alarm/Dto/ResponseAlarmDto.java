package com.example.LifeMaster_BE.TimeManager.Alarm.Dto;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum.RandomMissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAlarmDto {

    private Long id;
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

    // ==== 랜덤 미션 타입 ====
    @Schema(
            description = """
                랜덤 미션 유형
                - MATH_PROBLEM: 수학 문제 풀기
                - TYPING_SENTENCE: 문장 따라쓰기
                - FOLLOW_CLICK: 따라 누르기
                - NONE: 미션 없음
            """,
            example = "MATH_PROBLEM"
    )
    private RandomMissionType randomMissionType;

    // ==== 미션 난이도 ====
    @Schema(description = "랜덤 미션 난이도 (HIGH / MEDIUM / LOW)", example = "HIGH")
    private AlarmEntity.MissionLevel missionLevel;

    // ==== 수학 문제 미션 ====
    private String mathQuestion;   // 예: "12 + 8"
    private Integer mathAnswer;    // 예: 20

    // ==== 문장 따라쓰기 미션 ====
    private String typingSentence;

    // ==== 따라 누르기(그리드) 미션 ====
    private String followClickGridJson;  // JSON 문자열로 저장
}
