package com.example.LifeMaster_BE.TimeManager.Alarm.Dto;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum.RandomMissionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private boolean alarmStatus;

    private String alarmSound;

    private boolean snoozed;
    private int snoozeTime;
    private Long snoozeCount;

    private boolean reSleptPrevention;
    private int reSleptPreventionTime;

    // ==== 랜덤 미션 타입 ====
    @Schema(
            description = """
                랜덤 미션 유형
                - MATH_PROBLEM: 수학 문제 풀기
                - TYPING_SENTENCE: 문장 따라쓰기
                - FOLLOW_CLICK: 따라 누르기
                - NULL: 미션 없음
            """,
            example = "MATH_PROBLEM"
    )
    private RandomMissionType randomMissionType;

    // ==== 미션 난이도 ====
    @Schema(description = "랜덤 미션 난이도 (HIGH / MEDIUM / LOW)", example = "HIGH")
    private AlarmEntity.MissionLevel missionLevel;

    // ==== 수학 문제 미션 ====
    @JsonIgnore
    private String mathQuestion;   // 예: "12 + 8"

    @JsonIgnore
    private Integer mathAnswer;    // 예: 20

    // ==== 문장 따라쓰기 미션 ====
    @JsonIgnore
    private String typingSentence;

    // ==== 따라 누르기(그리드) 미션 ====
    @JsonIgnore
    private String followClickGridJson;  // JSON 문자열로 저장

    public static ResponseAlarmDto fromEntity(AlarmEntity alarm) {
        return new ResponseAlarmDto(
                alarm.getId(),
                alarm.getAlarmTitle(),
                alarm.getAlarmTime(),

                alarm.isAlarmMon(),
                alarm.isAlarmTue(),
                alarm.isAlarmWed(),
                alarm.isAlarmThu(),
                alarm.isAlarmFri(),
                alarm.isAlarmSat(),
                alarm.isAlarmSun(),

                alarm.isAlarmStatus(),
                alarm.getAlarmSound(),

                alarm.isSnoozed(),
                alarm.getSnoozeTime(),
                alarm.getSnoozeCount(),

                alarm.isReSleptPrevention(),
                alarm.getReSleptPreventionTime(),

                alarm.getRandomMissionType(),
                alarm.getMissionLevel(),

                // JsonIgnore라 응답엔 안 나감 (문제 없음)
                alarm.getMathQuestion(),
                alarm.getMathAnswer(),
                alarm.getTypingSentence(),
                alarm.getFollowClickGridJson()
        );
    }
}
