package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/detox/time")
public class TimeDetoxController {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MemberRepository memberRepository;
    private final Login login;
    private final ScheduleCalendarService scheduleCalendarService;

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
                    @ApiResponse(responseCode = "400", description = "현재 시간이 디톡스 활성화 시간 범위에 포함되어 있어 비활성화할 수 없음"),
                    @ApiResponse(responseCode = "404", description = "해당 ID의 스케줄을 찾을 수 없음")
            }
    )
    @PatchMapping("/{id}/toggle-activation")
    public ResponseEntity<?> toggleActivation(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        try {
            ZonedDateTime now = ZonedDateTime.now(KST);
            String currentDay = now.getDayOfWeek().name();
            LocalTime currentTime = now.toLocalTime();

            TimeDetoxEntity updatedSchedule =
                    service.toggleActivation(memberId, id, currentDay, currentTime);

            scheduleCalendarService.addOrUpdateEvent(memberId, todayKey(), "Detox");

            return ResponseEntity.ok(updatedSchedule);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "앱 잠금 상태 및 잠긴 앱 목록 확인",
            description = "한국 시간 기준 현재 날짜와 시간을 기준으로 앱 잠금 여부와 잠긴 앱 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "잠금 상태 및 목록 반환 성공",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스입니다."),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터")
            }
    )
    @GetMapping("/lock-status")
    public ResponseEntity<?> isAppLockedWithDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(userDetails);
        if (loginCheck != null) return loginCheck;

        ZonedDateTime now = ZonedDateTime.now(KST);

        String day = now.getDayOfWeek().name();
        LocalTime currentTime = now.toLocalTime();

        TimeDetoxService.LockedAppDetails details =
                service.isAppLockedWithDetails(userDetails.getId(), day, currentTime);

        return ResponseEntity.ok(details);
    }

    @Operation(
            summary = "새로운 시간 잠금 디톡스 생성",
            description = "로그인한 사용자의 새로운 시간 잠금 디톡스 일정을 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "시간 잠금 디톡스 일정 세부 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TimeDetoxDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "cycle": "WEEKLY",
                                                "day": "MONDAY",
                                                "startTime": "10:30",
                                                "endTime": "18:30",
                                                "lockedApps": "YouTube,Instagram,Facebook"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "시간 잠금 디톡스 생성 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxDto.class))),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스입니다."),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터")
            }
    )
    @PostMapping
    public ResponseEntity<?> createSchedule(
            @RequestBody TimeDetoxDto scheduleDto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        try {
            Long memberId = user.getId();

            TimeDetoxDto createdSchedule = service.createSchedule(scheduleDto, memberId);

            scheduleCalendarService.addOrUpdateEvent(memberId, todayKey(), "Detox");

            return ResponseEntity.ok(createdSchedule);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "특정 시간 잠금 디톡스 조회",
            description = "로그인한 사용자의 특정 시간 잠금 디톡스 일정을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "시간 잠금 디톡스 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxEntity.class))),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스입니다."),
                    @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getScheduleById(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        return ResponseEntity.ok(service.getScheduleById(user.getId(), id));
    }

    @Operation(
            summary = "내 시간 잠금 디톡스 전체 조회",
            description = "로그인한 사용자의 시간 잠금 디톡스를 전체 조회합니다. 비상탈출로 오늘만 비활성화된 일정은 disabledToday = true로 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 시간 잠금 디톡스 목록 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxDto.class))),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스입니다.")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<?> getMyTimeDetoxSchedules(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        List<TimeDetoxDto> schedules =
                service.getAllTimeDetoxSchedulesByMember(memberId);

        return ResponseEntity.ok(schedules);
    }

    @Operation(
            summary = "특정 시간 잠금 디톡스 수정",
            description = "로그인한 사용자의 특정 시간 잠금 디톡스 일정의 세부 정보를 수정합니다. 활성/비활성 상태는 수정 대상에서 제외됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정된 시간 잠금 디톡스 일정 세부 정보",
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
                    @ApiResponse(responseCode = "200", description = "시간 잠금 디톡스 수정 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxEntity.class))),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스입니다."),
                    @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음"),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable(name = "id") Long id,
            @RequestBody TimeDetoxEntity updatedSchedule,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        try {
            Long memberId = user.getId();

            String dateKey = todayKey();

            if (updatedSchedule.getDate() != null && !updatedSchedule.getDate().isBlank()) {
                dateKey = updatedSchedule.getDate();
            }

            scheduleCalendarService.addOrUpdateEvent(memberId, dateKey, "Detox");

            return ResponseEntity.ok(service.updateSchedule(memberId, id, updatedSchedule));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "본인 시간 잠금 디톡스 삭제",
            description = "ID를 기준으로 로그인한 사용자의 시간 잠금 디톡스 일정을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "시간 잠금 디톡스 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스입니다."),
                    @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        try {
            service.deleteSchedule(memberId, id);

            scheduleCalendarService.addOrUpdateEvent(memberId, todayKey(), "Detox");

            return ResponseEntity.noContent().build();

        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(404)
                    .body("삭제 실패: 스케줄이 없거나 삭제 권한이 없습니다.");
        }
    }

    @Operation(
            summary = "비상 탈출 문장 생성",
            description = "시간 잠금 디톡스 비상탈출에 필요한 문장을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비상 탈출 문장 생성 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스입니다.")
            }
    )
    @GetMapping("/generate-phrase")
    public ResponseEntity<String> generateRandomPhrase() {

        Long memberId = getCurrentMemberIdOrNull();

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        String phrase = service.generateRandomPhrase(memberId);

        return ResponseEntity.ok(phrase);
    }

    @Operation(
            summary = "비상 탈출 문장 검증",
            description = "비상 탈출 문장을 검증하고, 현재 실행 중인 오늘의 시간 잠금만 비활성화합니다. 반복 스케줄 자체는 종료하지 않습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "오늘의 시간 잠금 비활성화 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스입니다."),
                    @ApiResponse(responseCode = "400", description = "문장이 일치하지 않음")
            }
    )
    @PostMapping("/verify-phrase")
    public ResponseEntity<String> verifyPhraseAndEndDetox(
            @RequestBody String inputPhrase,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long memberId = user.getId();

        boolean result = service.verifyPhraseAndEndDetox(memberId, inputPhrase);

        if (result) {
            scheduleCalendarService.addOrUpdateEvent(memberId, todayKey(), "Detox");

            return ResponseEntity.ok("Today's time detox has been disabled successfully.");
        }

        return ResponseEntity.badRequest().body("Incorrect phrase. Detox remains active.");
    }

    private String todayKey() {
        return LocalDate.now(KST).format(DATE_KEY_FORMATTER);
    }

    private Long getCurrentMemberIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }

        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }

        if (principal instanceof MemberEntity me) {
            return me.getId();
        }

        return null;
    }
}