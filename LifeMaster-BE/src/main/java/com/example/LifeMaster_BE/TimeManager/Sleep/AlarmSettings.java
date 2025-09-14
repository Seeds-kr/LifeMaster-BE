// AlarmSettings.java
package com.example.LifeMaster_BE.TimeManager.Sleep;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class AlarmSettings {
    @Column(nullable = true)
    private Integer alarmSnoozeCnt;   // 알람 미루기 횟수
    @Column(nullable = true)
    private Integer timeToWakeUp;     // 일어나는데 걸린 시간(분)
    @Column(nullable = true)
    private Boolean antiSleepMode;    // 재수면 방지 여부
}
