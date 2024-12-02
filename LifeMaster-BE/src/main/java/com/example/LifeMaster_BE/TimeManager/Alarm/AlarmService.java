package com.example.LifeMaster_BE.TimeManager.Alarm;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public List<AlarmEntity> getAllAlarms(){
        return alarmRepository.findAll();
    }

    public AlarmEntity getAlarmById(Long alarmId){
        return alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm" + alarmId + "not found"));
    }

    public AlarmEntity updateBooleanField(Long alarmId, String fieldName, boolean status){
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm" + alarmId + "not found"));

        try{
            Field declaredField = AlarmEntity.class.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(alarm, status);
        }catch (NoSuchFieldException e){
            throw new IllegalArgumentException("Field " + fieldName + " not found", e);
        }catch (IllegalAccessException e){
            throw new RuntimeException("Failed to update field: " + fieldName, e);
        }

        return alarmRepository.save(alarm);
    }

    public String getTimeDifference(Long alarmId) {
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm " + alarmId + " not found"));

        LocalDateTime alarmTime = alarm.getAlarmTime();
        LocalDateTime now = LocalDateTime.now();

        long days = ChronoUnit.DAYS.between(now, alarmTime);
        long hours = ChronoUnit.HOURS.between(now, alarmTime) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, alarmTime) % 60;

        return String.format("Time difference: %d days, %d hours, %d minutes", days, hours, minutes);
    }

    public AlarmEntity createAlarm(AlarmEntity newAlarm) {
        return alarmRepository.save(newAlarm);
    }

}
