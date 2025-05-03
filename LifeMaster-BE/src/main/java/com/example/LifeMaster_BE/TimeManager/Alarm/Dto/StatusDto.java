package com.example.LifeMaster_BE.TimeManager.Alarm.Dto;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmDay;
import lombok.*;

@Data
@NoArgsConstructor
public class StatusDto {
    private boolean status;
    private AlarmDay day;
}
