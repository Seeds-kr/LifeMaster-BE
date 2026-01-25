package com.example.LifeMaster_BE.TimeManager.Alarm;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.AlarmMissionService;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum.RandomMissionType;
import com.example.LifeMaster_BE.TimeManager.Alarm.Mapper.AlarmMapStruct;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.NewAlarmDto;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.ResponseAlarmDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.*;
import java.time.format.DateTimeFormatter;
@Slf4j
@Service
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final AlarmMapStruct alarmMapStruct;
    private final ScheduleCalendarService scheduleCalendarService;
    private final AlarmMissionService alarmMissionService;

    public AlarmService(AlarmRepository alarmRepository, MemberRepository memberRepository, AlarmMapStruct alarmMapStruct, ScheduleCalendarService scheduleCalendarService, @Lazy AlarmMissionService alarmMissionService) {
        this.alarmRepository = alarmRepository;
        this.memberRepository = memberRepository;
        this.alarmMapStruct = alarmMapStruct;
        this.scheduleCalendarService = scheduleCalendarService;
        this.alarmMissionService = alarmMissionService;
    }

    public ResponseAlarmDto createAlarmAndSyncCalendar(
            NewAlarmDto alarmDto, Long memberId) {

        return createAlarm(alarmDto, memberId);
    }

    //알람 생성 메소드
    public ResponseAlarmDto createAlarm(NewAlarmDto alarmDto, Long memberId) {

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member " + memberId + " not found"));

        AlarmEntity newAlarm = AlarmEntity.fromDto(alarmDto);
        member.addAlarm(newAlarm);

        // 1) 먼저 저장 (alarmId 확보)
        AlarmEntity saved = alarmRepository.save(newAlarm);

        // 3) 캘린더 이벤트 생성 로직은 그대로
        ZoneId KST = ZoneId.of("Asia/Seoul");
        ZonedDateTime nowKst = ZonedDateTime.now(KST);

        LocalDate startDate = nowKst.toLocalDate();
        LocalDate endDate = YearMonth.from(startDate).atEndOfMonth();

        LocalTime alarmTimeOfDay = saved.getAlarmTime()
                .atZone(KST)
                .toLocalTime();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {

            DayOfWeek dow = d.getDayOfWeek();
            boolean enabled = switch (dow) {
                case MONDAY    -> saved.isAlarmMon();
                case TUESDAY   -> saved.isAlarmTue();
                case WEDNESDAY -> saved.isAlarmWed();
                case THURSDAY  -> saved.isAlarmThu();
                case FRIDAY    -> saved.isAlarmFri();
                case SATURDAY  -> saved.isAlarmSat();
                case SUNDAY    -> saved.isAlarmSun();
            };
            if (!enabled) continue;

            // 오늘은 "현재시간 이후"만 포함
            if (d.equals(startDate) && !alarmTimeOfDay.isAfter(nowKst.toLocalTime())) continue;

            String dateKey = d.format(fmt);

            scheduleCalendarService.addOrUpdateEvent(memberId, dateKey, "Alarm");
        }

        return ResponseAlarmDto.fromEntity(saved);
    }

    //알람 전체 조회 메소드
    public List<ResponseAlarmDto> getAllAlarms(){
        List<AlarmEntity> allAlarms = alarmRepository.findAll();
        return alarmMapStruct.toDtoList(allAlarms);
    }

    // 현재 로그인한 사용자의 알람만 조회
    public List<ResponseAlarmDto> getAlarmsByMember(Long memberId) {
        List<AlarmEntity> alarms = alarmRepository.findByMember_Id(memberId);
        return alarmMapStruct.toDtoList(alarms);
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
    public ResponseAlarmDto updateAlarm(Long alarmId, NewAlarmDto dto) {

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

        // @Transactional 이므로 save 생략 가능 (남겨도 무방)
        // alarmRepository.save(alarm);

        return ResponseAlarmDto.fromEntity(alarm);
    }

    public ResponseAlarmDto updateAlarmAndSyncCalendar(Long alarmId, NewAlarmDto dto, Long memberId) {

        ZoneId KST = ZoneId.of("Asia/Seoul");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 1) 수정 전 알람 로드 (old 스냅샷)
        AlarmEntity old = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm " + alarmId + " not found"));

        // old 스냅샷 (updateAlarm에서 엔티티 변경 전에 저장)
        LocalTime oldTimeOfDay = old.getAlarmTime().atZone(KST).toLocalTime();
        boolean oldMon = old.isAlarmMon();
        boolean oldTue = old.isAlarmTue();
        boolean oldWed = old.isAlarmWed();
        boolean oldThu = old.isAlarmThu();
        boolean oldFri = old.isAlarmFri();
        boolean oldSat = old.isAlarmSat();
        boolean oldSun = old.isAlarmSun();

        // 2) 실제 수정(기존 로직 재사용) + DTO 받기
        ResponseAlarmDto updatedDto = updateAlarm(alarmId, dto);

        // 3) 수정 후 알람 엔티티는 old가 같은 영속성 컨텍스트에서 갱신되어 있음
        //    (= 다시 findById 할 필요 없음)
        AlarmEntity updated = old;

        LocalTime newTimeOfDay = updated.getAlarmTime().atZone(KST).toLocalTime();

        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        LocalDate startDate = nowKst.toLocalDate();
        LocalDate endDate = YearMonth.from(startDate).atEndOfMonth();

        // 멤버의 다른 알람들(삭제 여부 판단용)
        List<AlarmEntity> memberAlarms = alarmRepository.findByMember_Id(memberId);

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {

            DayOfWeek dow = d.getDayOfWeek();

            boolean oldEnabled = enabledBySnapshot(dow, oldMon, oldTue, oldWed, oldThu, oldFri, oldSat, oldSun);
            boolean newEnabled = enabledByEntity(updated, dow);

            // '오늘'은 현재시간 이후만 포함 (old/new 각각의 알람 시각 기준)
            if (d.equals(startDate)) {
                boolean oldTimeOk = oldTimeOfDay.isAfter(nowKst.toLocalTime());
                boolean newTimeOk = newTimeOfDay.isAfter(nowKst.toLocalTime());

                oldEnabled = oldEnabled && oldTimeOk;
                newEnabled = newEnabled && newTimeOk;
            }

            // 변동 없음이면 skip
            if (oldEnabled == newEnabled) continue;

            String dateKey = d.format(fmt);

            if (!oldEnabled && newEnabled) {
                scheduleCalendarService.addOrUpdateEvent(memberId, dateKey, "Alarm");
                continue;
            }

            if (oldEnabled && !newEnabled) {
                boolean hasOtherAlarmThatDay = hasAnyOtherAlarmOnDate(
                        memberAlarms, alarmId, d, nowKst.toLocalTime()
                );

                if (!hasOtherAlarmThatDay) {
                    scheduleCalendarService.deleteSpecificEvent(memberId, dateKey, "Alarm");
                } else {
                    scheduleCalendarService.addOrUpdateEvent(memberId, dateKey, "Alarm");
                }
            }
        }

        // 컨트롤러가 받도록 최종 DTO 반환
        return updatedDto;
    }

    private boolean enabledBySnapshot(
            DayOfWeek day,
            boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat, boolean sun
    ) {
        return switch (day) {
            case MONDAY -> mon;
            case TUESDAY -> tue;
            case WEDNESDAY -> wed;
            case THURSDAY -> thu;
            case FRIDAY -> fri;
            case SATURDAY -> sat;
            case SUNDAY -> sun;
        };
    }

    private boolean enabledByEntity(AlarmEntity alarm, DayOfWeek day) {
        return switch (day) {
            case MONDAY    -> alarm.isAlarmMon();
            case TUESDAY   -> alarm.isAlarmTue();
            case WEDNESDAY -> alarm.isAlarmWed();
            case THURSDAY  -> alarm.isAlarmThu();
            case FRIDAY    -> alarm.isAlarmFri();
            case SATURDAY  -> alarm.isAlarmSat();
            case SUNDAY    -> alarm.isAlarmSun();
        };
    }

    /**
     * "해당 날짜에 다른 알람이 있는지" 판단
     * - 동일 member의 알람 중에서
     * - (현재 수정중인 alarmId 제외)
     * - 그 날짜의 요일 플래그가 true
     * - 그리고 날짜가 '오늘'이면 현재시간 이후인 알람만 인정(생성 로직과 동일 기준)
     */
    private boolean hasAnyOtherAlarmOnDate(
            List<AlarmEntity> memberAlarms,
            Long excludeAlarmId,
            LocalDate date,
            LocalTime nowTimeKst
    ) {
        DayOfWeek dow = date.getDayOfWeek();
        boolean isToday = date.equals(LocalDate.now(ZoneId.of("Asia/Seoul")));

        for (AlarmEntity a : memberAlarms) {
            if (a.getId() == null) continue;
            if (a.getId().equals(excludeAlarmId)) continue;

            if (!enabledByEntity(a, dow)) continue;

            if (isToday) {
                LocalTime t = a.getAlarmTime().atZone(ZoneId.of("Asia/Seoul")).toLocalTime();
                if (!t.isAfter(nowTimeKst)) continue;
            }

            return true;
        }
        return false;
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

    public void deleteAlarmAndSyncCalendar(Long alarmId, Long memberId) {
        ZoneId KST = ZoneId.of("Asia/Seoul");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 삭제 전: 알람 정보 확보(권한 체크 포함)
        ResponseAlarmDto alarmDto = getAlarmById(alarmId, memberId);

        // 알람 시각(오늘 now 이후 필터용)
        LocalTime alarmTimeOfDay = alarmDto.getAlarmTime().atZone(KST).toLocalTime();

        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        LocalDate startDate = nowKst.toLocalDate();                 // 오늘 포함
        LocalDate endDate = YearMonth.from(startDate).atEndOfMonth(); // 이번 달 말

        // 이 알람이 영향을 주는 dateKey 목록(삭제 후에는 못 구할 수 있으니 먼저 계산)
        List<String> affectedDateKeys = new ArrayList<>();

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();

            boolean enabled = switch (dow) {
                case MONDAY    -> alarmDto.isAlarmMon();
                case TUESDAY   -> alarmDto.isAlarmTue();
                case WEDNESDAY -> alarmDto.isAlarmWed();
                case THURSDAY  -> alarmDto.isAlarmThu();
                case FRIDAY    -> alarmDto.isAlarmFri();
                case SATURDAY  -> alarmDto.isAlarmSat();
                case SUNDAY    -> alarmDto.isAlarmSun();
            };
            if (!enabled) continue;

            // 오늘은 "현재시간 이후"만 포함
            if (d.equals(startDate) && !alarmTimeOfDay.isAfter(nowKst.toLocalTime())) continue;

            affectedDateKeys.add(d.format(fmt));
        }

        // 알람 삭제
        deleteAlarm(alarmId);

        // 각 날짜별: 같은 memberId 기준 해당일에 다른 알람이 0개면 Alarm 이벤트 삭제
        for (String dateKey : affectedDateKeys) {
            if (countAlarmsOnDate(memberId, dateKey) == 0) {
                scheduleCalendarService.deleteSpecificEvent(memberId,dateKey, "Alarm");
            }
        }
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

    // 내 알람 전체 활성화 (OFF → ON)
    public int activateMyAlarms(Long memberId) {
        List<AlarmEntity> alarms = alarmRepository.findByMember_Id(memberId);
        int count = 0;

        for (AlarmEntity alarm : alarms) {
            if (!alarm.isAlarmStatus()) {
                alarm.setAlarmStatus(true);
                count++;
            }
        }

        alarmRepository.saveAll(alarms);
        log.info("Activated {} alarms for member {}", count, memberId);
        return count;
    }

    // 내 알람 전체 비활성화 (ON → OFF)
    public int deactivateMyAlarms(Long memberId) {
        List<AlarmEntity> alarms = alarmRepository.findByMember_Id(memberId);
        int count = 0;

        for (AlarmEntity alarm : alarms) {
            if (alarm.isAlarmStatus()) {
                alarm.setAlarmStatus(false);
                count++;
            }
        }

        alarmRepository.saveAll(alarms);
        log.info("Deactivated {} alarms for member {}", count, memberId);
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
