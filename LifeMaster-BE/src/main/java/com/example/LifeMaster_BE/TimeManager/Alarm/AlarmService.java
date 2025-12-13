package com.example.LifeMaster_BE.TimeManager.Alarm;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum.RandomMissionType;
import com.example.LifeMaster_BE.TimeManager.Alarm.Mapper.AlarmMapStruct;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.NewAlarmDto;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.ResponseAlarmDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final AlarmMapStruct alarmMapStruct;

    //알람 생성 메소드
    public Long createAlarm(NewAlarmDto alarmDto, Long memberId) {

        // 알람 생성 시에는 미션 정보는 받지 않고, 항상 기본값으로 설정
        alarmDto.setRandomMissionType(null);
        alarmDto.setMissionLevel(null);

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member " + memberId + " not found"));

        AlarmEntity newAlarm = AlarmEntity.fromDto(alarmDto);

        member.addAlarm(newAlarm);
        AlarmEntity savedAlarm = alarmRepository.save(newAlarm);
        return savedAlarm.getId();
    }

    //알람 전체 조회 메소드
    public List<ResponseAlarmDto> getAllAlarms(){
        List<AlarmEntity> allAlarms = alarmRepository.findAll();
        return alarmMapStruct.toDtoList(allAlarms);
    }

    //특정 알람 조회 메소드
    public ResponseAlarmDto getAlarmById(Long alarmId, Long memberId){
        AlarmEntity alarm = alarmRepository.findByIdAndMemberId(alarmId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm" + alarmId + "not found"));

        return alarmMapStruct.toDto(alarm);
    }

    // 알람 상태 수정 매소드
    public void updateAlarmStatus(Long alarmId, boolean status){
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm" + alarmId + "not found"));

        alarm.setAlarmStatus(status);
        alarmRepository.save(alarm);
    }

    //알람 날짜 수정 메소드
    public void updateAlarmDayStatus(Long alarmId, AlarmDay day, boolean status){
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm" + alarmId + "not found"));

        switch (day) {
            case MON -> alarm.setAlarmMon(status);
            case TUE -> alarm.setAlarmTue(status);
            case WED -> alarm.setAlarmWed(status);
            case THU -> alarm.setAlarmThu(status);
            case FRI -> alarm.setAlarmFri(status);
            case SAT -> alarm.setAlarmSat(status);
            case SUN -> alarm.setAlarmSun(status);
        }

        alarmRepository.save(alarm);
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

    private void validateMission(NewAlarmDto dto) {
        RandomMissionType type = dto.getRandomMissionType();
        AlarmEntity.MissionLevel level = dto.getMissionLevel();

        if (type == null) {
            dto.setRandomMissionType(null);
            dto.setMissionLevel(null);
            return;
        }

        // === 난이도가 필요한 미션 목록 ===
        boolean needsLevel =
                type == RandomMissionType.MATH_PROBLEM || type == RandomMissionType.FOLLOW_CLICK;

        // 1) 난이도가 필요한데 level 없음 → 잘못된 요청
        if (needsLevel && level == null) {
            throw new IllegalArgumentException(
                    type + " 미션에는 missionLevel(HIGH/MEDIUM/LOW)이 필요합니다."
            );
        }

        // 2) 난이도가 필요 없는데 level 들어온 경우 → 무시
        if (!needsLevel && level != null) {
            dto.setMissionLevel(null);
        }
    }

    // 특정 알람 상태 토글
    public boolean toggleAlarm(Long alarmId) {
        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm " + alarmId + " not found"));

        boolean newStatus = !alarm.isAlarmStatus();
        alarm.setAlarmStatus(newStatus);

        alarmRepository.save(alarm);
        return newStatus; // 변경된 상태 반환 (프론트에서 쓰기 좋음)
    }

    // 전체 알람 활성화 (OFF → ON)
    public int activateAllAlarms() {
        List<AlarmEntity> alarms = alarmRepository.findAll();
        int count = 0;

        for (AlarmEntity alarm : alarms) {
            if (!alarm.isAlarmStatus()) {
                alarm.setAlarmStatus(true);
                count++;
            }
        }

        alarmRepository.saveAll(alarms);
        log.info("Activated {} alarms.", count);
        return count;
    }

    // 전체 알람 비활성화 (ON → OFF)
    public int deactivateAllAlarms() {
        List<AlarmEntity> alarms = alarmRepository.findAll();
        int count = 0;

        for (AlarmEntity alarm : alarms) {
            if (alarm.isAlarmStatus()) {
                alarm.setAlarmStatus(false);
                count++;
            }
        }

        alarmRepository.saveAll(alarms);
        log.info("Deactivated {} alarms.", count);
        return count;
    }

    public Optional<AlarmEntity> findAlarmById(Long alarmId) {
        return alarmRepository.findById(alarmId);
    }

    public AlarmEntity saveAlarm(AlarmEntity alarm) {
        return alarmRepository.save(alarm);
    }



}
