package com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission;


import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum.RandomMissionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlarmMissionAnswerResponseDto {
    private Long alarmId;
    private RandomMissionType missionType;
    private AlarmEntity.MissionLevel missionLevel;

    // 수학만 사용
    private String question;        // mathQuestion
    private Integer mathAnswer;     // mathAnswer

    // 타이핑/그리드용 (answer 쪽 역할)
    private String answerPayload;   // typingSentence or followClickGridJson
}

