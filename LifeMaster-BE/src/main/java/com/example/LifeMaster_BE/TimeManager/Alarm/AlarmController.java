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
import java.time.ZoneId;
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
    public ResponseEntity<ResponseAlarmDto> createAlarm(
            @RequestBody NewAlarmDto alarmDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        ResponseAlarmDto saved =
                alarmService.createAlarmAndSyncCalendar(alarmDto, memberId);

        URI location = URI.create("/time/alarm/" + saved.getId());

        return ResponseEntity
                .created(location)
                .body(saved);
    }


    @Operation(
            summary = "모든 알람 조회 (관리자 전용)",
            description = """
    등록된 모든 알람을 반환합니다.
    
    ⚠️ **관리자 전용 API**
    - 모든 사용자의 알람 정보를 조회합니다.
    - 일반 사용자는 사용하면 안 됩니다.
    
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 알람 조회 완료 (관리자)")
    })
    @GetMapping
    public ResponseEntity<List<ResponseAlarmDto>> getAllAlarms() {
        List<ResponseAlarmDto> allAlarms = alarmService.getAllAlarms();
        return ResponseEntity.ok(allAlarms);
    }

    @Operation(
            summary = "내 알람 조회",
            description = """
    현재 로그인한 사용자가 등록한 알람만 반환합니다.
    
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
    @GetMapping("/me")
    public ResponseEntity<List<ResponseAlarmDto>> getMyAlarms(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        List<ResponseAlarmDto> myAlarms = alarmService.getAlarmsByMember(memberId);
        return ResponseEntity.ok(myAlarms);
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
    public ResponseEntity<ResponseAlarmDto> updateAlarm(
            @PathVariable Long alarmId,
            @RequestBody NewAlarmDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();

        ResponseAlarmDto updated =
                alarmService.updateAlarmAndSyncCalendar(alarmId, dto, memberId);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{alarmId}")
    public ResponseEntity<String> deleteAlarm(
            @PathVariable("alarmId") Long alarmId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();

        alarmService.deleteAlarmAndSyncCalendar(alarmId, memberId);

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
     * 전체 알람 활성화 (OFF → ON) — 관리자 전용
     */
    @Operation(
            summary = "전체 알람 활성화 (관리자 전용)",
            description = """
    비활성화된 모든 알람을 활성화합니다.
    
    ⚠️ **관리자 전용 API**
    - 모든 사용자의 알람 상태에 영향을 줍니다.
    - 일반 사용자는 호출하면 안 됩니다.
    """
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
     * 전체 알람 비활성화 (ON → OFF) — 관리자 전용
     */
    @Operation(
            summary = "전체 알람 비활성화 (관리자 전용)",
            description = """
    활성화된 모든 알람을 비활성화합니다.
    
    ⚠️ **관리자 전용 API**
    - 모든 사용자의 알람 상태에 영향을 줍니다.
    - 일반 사용자는 호출하면 안 됩니다.
    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 알람 비활성화 완료")
    })
    @PatchMapping("/deactivate-all")
    public ResponseEntity<Integer> deactivateAllAlarms() {
        int deactivatedCount = alarmService.deactivateAllAlarms();
        return ResponseEntity.ok(deactivatedCount);
    }

    /**
     * 내 알람 전체 활성화 (OFF → ON)
     */
    @Operation(
            summary = "내 알람 전체 활성화",
            description = "현재 로그인한 사용자의 비활성화된 알람을 모두 활성화합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 알람 전체 활성화 완료")
    })
    @PatchMapping("/me/activate-all")
    public ResponseEntity<Integer> activateMyAlarms(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        int activatedCount = alarmService.activateMyAlarms(memberId);
        return ResponseEntity.ok(activatedCount);
    }

    /**
     * 내 알람 전체 비활성화 (ON → OFF)
     */
    @Operation(
            summary = "내 알람 전체 비활성화",
            description = "현재 로그인한 사용자의 활성화된 알람을 모두 비활성화합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 알람 전체 비활성화 완료")
    })
    @PatchMapping("/me/deactivate-all")
    public ResponseEntity<Integer> deactivateMyAlarms(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        int deactivatedCount = alarmService.deactivateMyAlarms(memberId);
        return ResponseEntity.ok(deactivatedCount);
    }
}
