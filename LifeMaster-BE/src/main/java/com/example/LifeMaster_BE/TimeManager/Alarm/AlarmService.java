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

    //알람 전체 조회 메소드
    public List<AlarmEntity> getAllAlarms(){
        return alarmRepository.findAll();
    }

    //특정 알람 조회 메소드
    public AlarmEntity getAlarmById(Long alarmId){
        return alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm" + alarmId + "not found"));
    }

    //알람 정보 수정 메소드
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

    //알람 남은 시간 계산 메소드
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

    //알람 생성 메소드
    public AlarmEntity createAlarm(AlarmEntity newAlarm) {
        return alarmRepository.save(newAlarm);
    }

    //알람 활성화 메소드
    public String activateAlarm(Long alarmId, String currentDay, LocalDateTime currentTime) {
        // 알람 ID로 알람 조회
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm with ID " + alarmId + " not found"));

        // 요일 및 시간 일치 확인
        boolean isDayMatch = switch (currentDay.toLowerCase()) {
            case "monday" -> alarm.isAlarmMon();
            case "tuesday" -> alarm.isAlarmTue();
            case "wednesday" -> alarm.isAlarmWed();
            case "thursday" -> alarm.isAlarmThu();
            case "friday" -> alarm.isAlarmFri();
            case "saturday" -> alarm.isAlarmSat();
            case "sunday" -> alarm.isAlarmSun();
            default -> false;
        };

        if (isDayMatch && alarm.getAlarmTime().toLocalTime().equals(currentTime.toLocalTime())) {
            // 알람 활성화
            alarm.setAlarmStatus(true);
            alarmRepository.save(alarm);
            log.info("Alarm {} activated.", alarm.getId());
            return "Alarm activated successfully.";
        } else {
            return "Alarm does not match the current day or time.";
        }
    }

    //알람 삭제 메소드
    public void deleteAlarm(Long alarmId) {
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm " + alarmId + " not found"));
        alarmRepository.delete(alarm);
    }



}
