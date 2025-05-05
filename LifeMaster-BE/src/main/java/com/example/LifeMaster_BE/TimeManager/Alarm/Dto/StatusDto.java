package com.example.LifeMaster_BE.TimeManager.Alarm.Dto;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmDay;
import lombok.*;

@Data
@NoArgsConstructor
public class StatusDto {
    private AlarmDay day;
    private boolean status;
}
