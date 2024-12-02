package com.example.LifeMaster_BE.TimeManager.Alarm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("/time/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public List<AlarmEntity> getAllAlarms(){
        return alarmService.getAllAlarms();
    }

    @GetMapping("/{alarmId}")
    public ResponseEntity<AlarmEntity> getAlarmById(@PathVariable Long alarmId){
        AlarmEntity alarmById = alarmService.getAlarmById(alarmId);
        return ResponseEntity.ok(alarmById);
    }

    @PutMapping("/{alarmId}/update-status")
    public ResponseEntity<AlarmEntity> setAlarmStatus(@PathVariable Long alarmId, @RequestBody StatusDto statusDto){
        AlarmEntity updatedAlarm = alarmService.updateBooleanField(alarmId, statusDto.getField(), statusDto.isStatus());
        return ResponseEntity.ok(updatedAlarm);
    }

    @PostMapping
    public ResponseEntity<AlarmEntity> createAlarm(@RequestBody AlarmEntity newAlarm) {
        AlarmEntity createdAlarm = alarmService.createAlarm(newAlarm);
        return ResponseEntity.ok(createdAlarm);
    }

    @GetMapping("/{alarmId}/time-difference")
    public ResponseEntity<String> getTimeDifference(@PathVariable("alarmId") Long alarmId) {
        String timeDifference = alarmService.getTimeDifference(alarmId);
        return ResponseEntity.ok(timeDifference);
    }
}
