package com.example.LifeMaster_BE.TimeManager.Alarm;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public String activateAlarm(Long alarmId) {
        // 알람 ID로 알람 조회
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm with ID " + alarmId + " not found"));

        // 현재 시스템 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        String currentDay = now.getDayOfWeek().name().toLowerCase(); // 현재 요일 (소문자로 변환)
        LocalTime currentTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES); // 초와 나노초 제거

        // 알람 시간도 분 단위로 변환
        LocalTime alarmTime = alarm.getAlarmTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);

        // 요일 일치 여부 확인
        boolean isDayMatch = switch (currentDay) {
            case "monday" -> alarm.isAlarmMon();
            case "tuesday" -> alarm.isAlarmTue();
            case "wednesday" -> alarm.isAlarmWed();
            case "thursday" -> alarm.isAlarmThu();
            case "friday" -> alarm.isAlarmFri();
            case "saturday" -> alarm.isAlarmSat();
            case "sunday" -> alarm.isAlarmSun();
            default -> false;
        };

        // 요일 및 시간 일치 여부 확인
        if (isDayMatch && alarmTime.equals(currentTime)) {
            // 알람 활성화
            alarm.setAlarmStatus(true);
            alarmRepository.save(alarm);
            log.info("Alarm {} activated.", alarm.getId());
            return "Alarm activated successfully.";
        } else {
            return "Alarm does not match the current day or time.";
        }
    }

    //전체 알람 활성화 메소드
    public String activateMatchingAlarms() {
        // 현재 시스템 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        String currentDay = now.getDayOfWeek().name().toLowerCase(); // 현재 요일 (소문자로 변환)
        LocalTime currentTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES); // 현재 시간 (분 단위로 변환)

        // 모든 알람 가져오기
        List<AlarmEntity> alarms = alarmRepository.findAll();

        // 활성화된 알람 카운트
        int activatedCount = 0;

        for (AlarmEntity alarm : alarms) {
            // 알람 시간도 분 단위로 변환
            LocalTime alarmTime = alarm.getAlarmTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);

            // 요일 일치 여부 확인
            boolean isDayMatch = switch (currentDay) {
                case "monday" -> alarm.isAlarmMon();
                case "tuesday" -> alarm.isAlarmTue();
                case "wednesday" -> alarm.isAlarmWed();
                case "thursday" -> alarm.isAlarmThu();
                case "friday" -> alarm.isAlarmFri();
                case "saturday" -> alarm.isAlarmSat();
                case "sunday" -> alarm.isAlarmSun();
                default -> false;
            };

            // 요일 및 시간 일치 여부 확인
            if (isDayMatch && alarmTime.equals(currentTime)) {
                // 알람 활성화
                alarm.setAlarmStatus(true);
                alarmRepository.save(alarm);
                activatedCount++;
                log.info("Alarm {} activated.", alarm.getId());
            }
        }

        // 결과 반환
        if (activatedCount > 0) {
            return activatedCount + " alarms activated successfully.";
        } else {
            return "No alarms matched the current day or time.";
        }
    }

    public String deactivateActivatedAlarms() {
        // 모든 알람 가져오기
        List<AlarmEntity> alarms = alarmRepository.findAll();

        // 비활성화된 알람 카운트
        int deactivatedCount = 0;

        for (AlarmEntity alarm : alarms) {
            // 활성화 상태인지 확인
            if (alarm.isAlarmStatus()) {
                // 비활성화 처리
                alarm.setAlarmStatus(false);
                alarmRepository.save(alarm);
                deactivatedCount++;
                log.info("Alarm {} deactivated.", alarm.getId());
            }
        }

        // 결과 반환
        if (deactivatedCount > 0) {
            return deactivatedCount + " alarms deactivated successfully.";
        } else {
            return "No active alarms to deactivate.";
        }
    }

    //알람 삭제 메소드
    public void deleteAlarm(Long alarmId) {
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm " + alarmId + " not found"));
        alarmRepository.delete(alarm);
    }



}
