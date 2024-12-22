package com.example.LifeMaster_BE.Challenge.Detox;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/detox/time")
public class TimeDetoxController {

    @Autowired
    private TimeDetoxService service;

    @Operation(
            summary = "새로운 디톡스 일정 생성",
            description = "새로운 디톡스 일정을 데이터베이스에 추가합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일정 세부 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TimeDetoxEntity.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "cycle": "WEEKLY",
                                        "day": "MONDAY",
                                        "startTime": "10:30:00",
                                        "endTime": "18:30:00",
                                        "active": true,
                                        "lockedApps": ["YouTube", "Instagram", "Facebook"]
                                    }
                                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정이 성공적으로 생성됨",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxEntity.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터")
            })
    @PostMapping
    public ResponseEntity<TimeDetoxEntity> createSchedule(@RequestBody TimeDetoxEntity schedule) {
        return ResponseEntity.ok(service.createSchedule(schedule));
    }

    @Operation(
            summary = "모든 디톡스 일정 조회",
            description = "데이터베이스에서 모든 디톡스 일정을 가져옵니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 목록 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxEntity.class)))
            })
    @GetMapping
    public ResponseEntity<List<TimeDetoxEntity>> getAllSchedules() {
        return ResponseEntity.ok(service.getAllSchedules());
    }

    @Operation(
            summary = "특정 디톡스 일정 조회",
            description = "ID를 기준으로 특정 디톡스 일정을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxEntity.class))),
                    @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
            })
    @GetMapping("/{id}")
    public ResponseEntity<TimeDetoxEntity> getScheduleById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(service.getScheduleById(id));
    }

    @Operation(
            summary = "특정 디톡스 일정 수정",
            description = "기존 디톡스 일정의 세부 정보를 수정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정된 일정 세부 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TimeDetoxEntity.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "cycle": "BIWEEKLY",
                                        "day": "TUESDAY",
                                        "startTime": "09:00:00",
                                        "endTime": "17:00:00",
                                        "isActive": false,
                                        "lockedApps": ["Twitter", "Netflix"]
                                    }
                                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 수정 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxEntity.class))),
                    @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음"),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터")
            })
    @PutMapping("/{id}")
    public ResponseEntity<TimeDetoxEntity> updateSchedule(@PathVariable(name = "id") Long id, @RequestBody TimeDetoxEntity updatedSchedule) {
        return ResponseEntity.ok(service.updateSchedule(id, updatedSchedule));
    }

    @Operation(
            summary = "특정 디톡스 일정 삭제",
            description = "ID를 기준으로 디톡스 일정을 데이터베이스에서 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "일정 삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable(name = "id") Long id) {
        service.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "앱 잠금 상태 확인",
            description = "현재 날짜와 시간을 기준으로 앱 잠금 여부를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "잠금 상태 반환",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터")
            })
    @GetMapping("/lock-status")
    public ResponseEntity<Boolean> isAppLocked() {
        // 현재 날짜와 시간을 가져옴
        LocalDateTime now = LocalDateTime.now();

        // 요일 (MONDAY, TUESDAY 등)을 가져옴
        String day = now.getDayOfWeek().name();

        // 시간 (HH:mm:ss)을 가져옴
        LocalTime currentTime = now.toLocalTime();

        // 서비스에서 잠금 상태 확인
        return ResponseEntity.ok(service.isAppLocked(day, currentTime));
    }
}
