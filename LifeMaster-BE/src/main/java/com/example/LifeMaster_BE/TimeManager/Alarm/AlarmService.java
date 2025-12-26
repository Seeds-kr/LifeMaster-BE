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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    public AlarmEntity createAlarm(NewAlarmDto alarmDto, Long memberId) {

        alarmDto.setRandomMissionType(null);
        alarmDto.setMissionLevel(null);

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member " + memberId + " not found"));

        AlarmEntity newAlarm = AlarmEntity.fromDto(alarmDto);

        member.addAlarm(newAlarm);

        // 저장된 엔티티를 그대로 반환
        return alarmRepository.save(newAlarm);
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

    //알람 수정 메소드
    @Transactional
    public void updateAlarm(Long alarmId, NewAlarmDto dto) {

        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Alarm " + alarmId + " not found")
                );

        // ===== 기본 정보 =====
        alarm.setAlarmTitle(dto.getAlarmTitle());
        alarm.setAlarmTime(dto.getAlarmTime());
        alarm.setAlarmStatus(dto.isAlarmStatus());
        alarm.setAlarmSound(dto.getAlarmSound());

        // ===== 요일 =====
        alarm.setAlarmMon(dto.isAlarmMon());
        alarm.setAlarmTue(dto.isAlarmTue());
        alarm.setAlarmWed(dto.isAlarmWed());
        alarm.setAlarmThu(dto.isAlarmThu());
        alarm.setAlarmFri(dto.isAlarmFri());
        alarm.setAlarmSat(dto.isAlarmSat());
        alarm.setAlarmSun(dto.isAlarmSun());

        // ===== 스누즈 =====
        alarm.setSnoozed(dto.isSnoozed());
        alarm.setSnoozeTime(dto.getSnoozeTime());
        alarm.setSnoozeCount(dto.getSnoozeCount());

        // ===== 재수면 방지 =====
        alarm.setReSleptPrevention(dto.isReSleptPrevention());
        alarm.setReSleptPreventionTime(dto.getReSleptPreventionTime());

        // ===== 미션 =====
        alarm.setRandomMissionType(dto.getRandomMissionType());
        alarm.setMissionLevel(dto.getMissionLevel());

        // @Transactional → save 생략 가능
        alarmRepository.save(alarm);
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

    public long countAlarmsOnDate(Long memberId, String yyyyMMdd) {
        LocalDate date = LocalDate.parse(yyyyMMdd, DateTimeFormatter.ofPattern("yyyyMMdd"));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return alarmRepository.countByMember_IdAndAlarmTimeBetween(memberId, start, end);
    }

    public Optional<AlarmEntity> findAlarmById(Long alarmId) {
        return alarmRepository.findById(alarmId);
    }

    public AlarmEntity saveAlarm(AlarmEntity alarm) {
        return alarmRepository.save(alarm);
    }



}
