package com.example.LifeMaster_BE.TimeManager.Alarm;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.NewAlarmDto;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.StatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/time/alarm")
@RequiredArgsConstructor
@Tag(name = "Alarm API", description = "알람 관련 API를 제공합니다.")
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "새 알람 생성", description = "새로운 알람을 생성합니다.")
    @PostMapping
    public ResponseEntity<Void> createAlarm(
            @RequestBody NewAlarmDto alarmDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        alarmService.createAlarm(alarmDto, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "모든 알람 조회", description = "등록된 모든 알람을 반환합니다.")
    @GetMapping
    public List<AlarmEntity> getAllAlarms() {
        return alarmService.getAllAlarms();
    }

    @Operation(summary = "특정 알람 조회", description = "알람 ID를 이용해 특정 알람의 상세 정보를 반환합니다.")
    @Parameter(name = "alarmId", description = "조회할 알람의 ID", required = true)
    @GetMapping("/{alarmId}")
    public ResponseEntity<AlarmEntity> getAlarmById(
            @PathVariable Long alarmId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        AlarmEntity alarmById = alarmService.getAlarmById(alarmId, memberId);
        return ResponseEntity.ok(alarmById);
    }

    @Operation(summary = "알람 상태 업데이트", description = "알람의 특정 필드(MON~SUN) 상태를 업데이트합니다.(ex day:MON, status:false")
    @Parameter(name = "alarmId", description = "업데이트할 알람의 ID", required = true)
    @PutMapping("/{alarmId}/update-status")
    public ResponseEntity<String> setAlarmStatus(
            @PathVariable("alarmId") Long alarmId,
            @RequestBody StatusDto statusDto) {
        alarmService.updateAlarmDayStatus(alarmId, statusDto.getDay(), statusDto.isStatus());
        return ResponseEntity.ok("정상적으로 업데이트 되었습니다.");
    }

    @Operation(summary = "알람 시간 차이 조회", description = "알람 시간과 현재 시간의 차이를 계산하여 반환합니다.")
    @Parameter(name = "alarmId", description = "시간 차이를 계산할 알람의 ID", required = true)
    @GetMapping("/{alarmId}/time-difference")
    public ResponseEntity<String> getTimeDifference(@PathVariable("alarmId") Long alarmId) {
        String timeDifference = alarmService.getTimeDifference(alarmId);
        return ResponseEntity.ok(timeDifference);
    }

    @Operation(summary = "특정 알람 활성화", description = "현재 시간을 기준으로 특정 알람을 활성화합니다.")
    @Parameter(name = "alarmId", description = "활성화할 알람의 ID", required = true)
    //@Parameter(name = "day", description = "현재 요일 (e.g., Monday, Tuesday)", required = true)
    //@Parameter(name = "time", description = "현재 시간 (ISO-8601 형식)", required = true)
    @PutMapping("/{alarmId}/activate")
    public ResponseEntity<String> activateAlarm(
            @PathVariable("alarmId") Long alarmId) {
        String response = alarmService.activateAlarm(alarmId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "알람 활성화", description = "현재 시간을 기준으로 해당하는 알람을 활성화합니다.")
    @PutMapping("/activate")
    public ResponseEntity<String> activateMatchAlarm() {
        String response = alarmService.activateMatchingAlarms();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "알람 비활성화", description = "활성화된 알람을 비활성화합니다.")
    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivateActivatedAlarms() {
        String result = alarmService.deactivateActivatedAlarms();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "알람 삭제", description = "특정 ID를 가진 알람을 삭제합니다.")
    @Parameter(name = "alarmId", description = "삭제할 알람의 ID", required = true)
    @DeleteMapping("/{alarmId}")
    public ResponseEntity<String> deleteAlarm(@PathVariable("alarmId") Long alarmId) {
        alarmService.deleteAlarm(alarmId);
        return ResponseEntity.ok("Alarm with ID " + alarmId + " has been deleted successfully.");
    }
}
