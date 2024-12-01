package com.example.LifeMaster_BE.TimeManager.Alarm;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
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
}
