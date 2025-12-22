package com.example.LifeMaster_BE.TimeManager.Alarm;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.NewAlarmDto;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.ResponseAlarmDto;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.StatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/time/alarm")
@RequiredArgsConstructor
@Tag(name = "Alarm API", description = "알람 관련 API를 제공합니다.")
public class AlarmController {

    private final AlarmService alarmService;
    private final ScheduleCalendarService scheduleCalendarService;

    @Operation(
            summary = "새 알람 생성",
            description = """
    새로운 알람을 생성합니다.
    
    **랜덤 미션 유형 (randomMissionType)**
    - `MATH_PROBLEM`: 수학 문제 풀기
    - `TYPING_SENTENCE`: 문장 따라쓰기
    - `FOLLOW_CLICK`: 따라 누르기 게임
    - `NULL`: 미션 없음 (기본값)
    """
    )
    @PostMapping
    public ResponseEntity<Void> createAlarm(
            @RequestBody NewAlarmDto alarmDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        Long alarmId = alarmService.createAlarm(alarmDto, memberId);

        // 오늘 날짜 "yyyyMMdd"로 변환
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(today, "Alarm");

        URI location = URI.create("/time/alarm/" + alarmId);
        return ResponseEntity.created(location).build();
    }


    @Operation(
            summary = "모든 알람 조회",
            description = """
    등록된 모든 알람을 반환합니다.
    
    **랜덤 미션 유형 (randomMissionType)**  
    - `MATH_PROBLEM`: 수학 문제 풀기  
    - `TYPING_SENTENCE`: 문장 따라쓰기  
    - `FOLLOW_CLICK`: 따라 누르기 게임  
    - `NULL`: 미션 없음  
    
    **난이도 (missionLevel)**  
    - `HIGH`: 어려움  
    - `MEDIUM`: 보통  
    - `LOW`: 쉬움  
    
    ※ 난이도는 `MATH_PROBLEM` 및 `FOLLOW_CLICK` 미션에서만 사용됩니다.
    """
    )
    @GetMapping
    public ResponseEntity<List<ResponseAlarmDto>> getAllAlarms() {
        List<ResponseAlarmDto> allAlarms = alarmService.getAllAlarms();
        return ResponseEntity.ok(allAlarms);
    }

    @Operation(summary = "특정 알람 조회", description = "알람 ID를 이용해 특정 알람의 상세 정보를 반환합니다.")
    @Parameter(name = "alarmId", description = "조회할 알람의 ID", required = true)
    @GetMapping("/{alarmId}")
    public ResponseEntity<ResponseAlarmDto> getAlarmById(
            @PathVariable Long alarmId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        ResponseAlarmDto alarmDtoById = alarmService.getAlarmById(alarmId, memberId);
        return ResponseEntity.ok(alarmDtoById);
    }

    @Operation(
            summary = "알람 수정",
            description = "NewAlarmDto 구조 기반으로 알람을 수정합니다."
    )
    @Parameter(name = "alarmId", description = "수정할 알람의 ID", required = true)
    @PutMapping("/{alarmId}")
    public ResponseEntity<String> updateAlarm(
            @PathVariable Long alarmId,
            @RequestBody NewAlarmDto dto
    ) {
        alarmService.updateAlarm(alarmId, dto);

        // 오늘 날짜 "yyyyMMdd"
        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 캘린더 이벤트 갱신
        scheduleCalendarService.addOrUpdateEvent(today, "Alarm");

        return ResponseEntity.ok("알람이 정상적으로 수정되었습니다.");
    }

    @Operation(summary = "알람 삭제", description = "특정 ID를 가진 알람을 삭제합니다.")
    @Parameter(name = "alarmId", description = "삭제할 알람의 ID", required = true)
    @DeleteMapping("/{alarmId}")
    public ResponseEntity<String> deleteAlarm(@PathVariable("alarmId") Long alarmId) {
        alarmService.deleteAlarm(alarmId);

        // 오늘 날짜 "yyyyMMdd"로 변환
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(today, "Alarm");

        return ResponseEntity.ok("Alarm with ID " + alarmId + " has been deleted successfully.");
    }

    /**
     * 특정 알람 상태 토글 (ON ↔ OFF)
     */
    @Operation(
            summary = "특정 알람 상태 토글",
            description = "지정한 알람의 상태를 활성화/비활성화로 전환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알람 상태 변경 성공"),
            @ApiResponse(responseCode = "404", description = "알람을 찾을 수 없음")
    })
    @PatchMapping("/{alarmId}/toggle")
    public ResponseEntity<Boolean> toggleAlarm(
            @PathVariable Long alarmId
    ) {
        boolean newStatus = alarmService.toggleAlarm(alarmId);
        return ResponseEntity.ok(newStatus);
    }

    /**
     * 전체 알람 활성화 (OFF → ON)
     */
    @Operation(
            summary = "전체 알람 활성화",
            description = "비활성화된 모든 알람을 활성화합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 알람 활성화 완료")
    })
    @PatchMapping("/activate-all")
    public ResponseEntity<Integer> activateAllAlarms() {
        int activatedCount = alarmService.activateAllAlarms();
        return ResponseEntity.ok(activatedCount);
    }

    /**
     * 전체 알람 비활성화 (ON → OFF)
     */
    @Operation(
            summary = "전체 알람 비활성화",
            description = "활성화된 모든 알람을 비활성화합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 알람 비활성화 완료")
    })
    @PatchMapping("/deactivate-all")
    public ResponseEntity<Integer> deactivateAllAlarms() {
        int deactivatedCount = alarmService.deactivateAllAlarms();
        return ResponseEntity.ok(deactivatedCount);
    }
}
