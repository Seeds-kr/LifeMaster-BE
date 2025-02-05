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
            summary = "디톡스 활성화/비활성화 전환",
            description = """
                    특정 디톡스 ID를 기반으로 활성화/비활성화를 전환합니다.
                    - 비활성 상태에서는 활성화로 전환됩니다.
                    - 활성 상태에서 현재 시간이 스케줄에 포함되지 않으면 비활성화됩니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 전환 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxEntity.class))),
                    @ApiResponse(responseCode = "404", description = "해당 ID의 스케줄을 찾을 수 없음")
            })
    @PatchMapping("/{id}/toggle-activation")
    public ResponseEntity<?> toggleActivation(@PathVariable(name = "id") Long id) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String currentDay = now.getDayOfWeek().name();
            LocalTime currentTime = now.toLocalTime();

            TimeDetoxEntity updatedSchedule = service.toggleActivation(id, currentDay, currentTime);
            return ResponseEntity.ok(updatedSchedule);
        } catch (IllegalStateException e) {
            // 비활성화 불가 사유 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "앱 잠금 상태 및 잠긴 앱 목록 확인",
            description = "현재 날짜와 시간을 기준으로 앱 잠금 여부와 잠긴 앱 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "잠금 상태 및 목록 반환 성공",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터")
            })
    @GetMapping("/lock-status")
    public ResponseEntity<TimeDetoxService.LockedAppDetails> isAppLockedWithDetails() {
        // 현재 날짜와 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        String day = now.getDayOfWeek().name(); // 요일 (MONDAY 등)
        LocalTime currentTime = now.toLocalTime(); // 현재 시간 (HH:mm:ss)

        // 서비스 호출
        TimeDetoxService.LockedAppDetails details = service.isAppLockedWithDetails(day, currentTime);
        return ResponseEntity.ok(details);
    }

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
                    @ApiResponse(responseCode = "200", description = "일정 생성 성공",
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
            summary = "특정 디톡스 일정 수정 (활성/비활성 제외)",
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

    @Operation(summary = "비상 탈출 문장 생성",
            description = "디톡스 비상 탈출에 필요한 문장을 생성합니다.")
    @GetMapping("/generate-phrase")
    public ResponseEntity<String> generateRandomPhrase() {
        String phrase = service.generateRandomPhrase();
        return ResponseEntity.ok(phrase);
    }

    @Operation(summary = "비상 탈출 문장 검증",
            description = "비상 탈출 문장을 검증하고, 실행 중인 디톡스를 종료합니다.")
    @PostMapping("/verify-phrase")
    public ResponseEntity<String> verifyPhraseAndEndDetox(@RequestBody String inputPhrase) {
        boolean result = service.verifyPhraseAndEndDetox(inputPhrase);
        if (result) {
            return ResponseEntity.ok("Detox has been successfully ended.");
        }
        return ResponseEntity.badRequest().body("Incorrect phrase. Detox remains active.");
    }
}
