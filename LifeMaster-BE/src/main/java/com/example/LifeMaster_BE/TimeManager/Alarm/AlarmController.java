package com.example.LifeMaster_BE.TimeManager.Alarm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/time/alarm")
@RequiredArgsConstructor
@Tag(name = "Alarm API", description = "알람 관련 API를 제공합니다.")
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "모든 알람 조회", description = "등록된 모든 알람을 반환합니다.")
    @GetMapping
    public List<AlarmEntity> getAllAlarms() {
        return alarmService.getAllAlarms();
    }

    @Operation(summary = "특정 알람 조회", description = "알람 ID를 이용해 특정 알람의 상세 정보를 반환합니다.")
    @Parameter(name = "alarmId", description = "조회할 알람의 ID", required = true)
    @GetMapping("/{alarmId}")
    public ResponseEntity<AlarmEntity> getAlarmById(@PathVariable Long alarmId) {
        AlarmEntity alarmById = alarmService.getAlarmById(alarmId);
        return ResponseEntity.ok(alarmById);
    }

    @Operation(summary = "알람 상태 업데이트", description = "알람의 특정 필드 상태를 업데이트합니다.")
    @Parameter(name = "alarmId", description = "업데이트할 알람의 ID", required = true)
    @PutMapping("/{alarmId}/update-status")
    public ResponseEntity<AlarmEntity> setAlarmStatus(
            @PathVariable("alarmId") Long alarmId,
            @RequestBody StatusDto statusDto) {
        AlarmEntity updatedAlarm = alarmService.updateBooleanField(alarmId, statusDto.getField(), statusDto.isStatus());
        return ResponseEntity.ok(updatedAlarm);
    }

    @Operation(summary = "새 알람 생성", description = "새로운 알람을 생성합니다.")
    @PostMapping
    public ResponseEntity<AlarmEntity> createAlarm(@RequestBody AlarmEntity newAlarm) {
        AlarmEntity createdAlarm = alarmService.createAlarm(newAlarm);
        return ResponseEntity.ok(createdAlarm);
    }

    @Operation(summary = "알람 시간 차이 조회", description = "알람 시간과 현재 시간의 차이를 계산하여 반환합니다.")
    @Parameter(name = "alarmId", description = "시간 차이를 계산할 알람의 ID", required = true)
    @GetMapping("/{alarmId}/time-difference")
    public ResponseEntity<String> getTimeDifference(@PathVariable("alarmId") Long alarmId) {
        String timeDifference = alarmService.getTimeDifference(alarmId);
        return ResponseEntity.ok(timeDifference);
    }

    @Operation(summary = "알람 활성화", description = "요일과 시간을 입력받아 특정 알람을 활성화합니다.")
    @Parameter(name = "alarmId", description = "활성화할 알람의 ID", required = true)
    @Parameter(name = "day", description = "현재 요일 (e.g., Monday, Tuesday)", required = true)
    @Parameter(name = "time", description = "현재 시간 (ISO-8601 형식)", required = true)
    @PutMapping("/{alarmId}/activate")
    public ResponseEntity<String> activateAlarm(
            @PathVariable("alarmId") Long alarmId,
            @RequestParam(name = "day") String currentDay,
            @RequestParam(name = "time") String currentTime) {
        LocalDateTime parsedTime = LocalDateTime.parse(currentTime); // ISO-8601 형식 입력 가정
        String response = alarmService.activateAlarm(alarmId, currentDay, parsedTime);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "알람 삭제", description = "특정 ID를 가진 알람을 삭제합니다.")
    @Parameter(name = "alarmId", description = "삭제할 알람의 ID", required = true)
    @DeleteMapping("/{alarmId}")
    public ResponseEntity<String> deleteAlarm(@PathVariable("alarmId") Long alarmId) {
        alarmService.deleteAlarm(alarmId);
        return ResponseEntity.ok("Alarm with ID " + alarmId + " has been deleted successfully.");
    }
}
