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

    // 문제/정답 (미션별로 사용)
    private String question;   // mathQuestion / typingSentence / followClickGridJson(문제 데이터)
    private Integer answer;    // mathAnswer (타이핑/그리드면 null)
}

