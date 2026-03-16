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
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/detox/time")
public class TimeDetoxController {

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
                    @ApiResponse(responseCode = "404", description = "해당 ID의 스케줄을 찾을 수 없음")
            })
    @PatchMapping("/{id}/toggle-activation")
    public ResponseEntity<?> toggleActivation(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        try {
            LocalDateTime now = LocalDateTime.now();
            String currentDay = now.getDayOfWeek().name();
            LocalTime currentTime = now.toLocalTime();

            // 내 스케줄만 토글 (memberId 포함)
            TimeDetoxEntity updatedSchedule = service.toggleActivation(memberId, id, currentDay, currentTime);

            // 오늘 날짜 "yyyyMMdd" (KST로 명확히 하고 싶으면 ZoneId.of("Asia/Seoul") 사용)
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 내 캘린더에만 이벤트 추가
            scheduleCalendarService.addOrUpdateEvent(memberId, today, "Detox");

            return ResponseEntity.ok(updatedSchedule);
        } catch (IllegalStateException e) {
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
                            schema = @Schema(implementation = TimeDetoxDTO.class),
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
                    @ApiResponse(responseCode = "200", description = "일정 생성 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxDTO.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터")
            })
    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody TimeDetoxDTO scheduleDto,
                                                       @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = user.getId();
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        TimeDetoxDTO createdSchedule = service.createSchedule(scheduleDto, memberId);

        // 오늘 날짜 "yyyyMMdd"로 변환
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(memberId,today, "Detox");

        return ResponseEntity.ok(createdSchedule);
    }

    @Operation(
            summary = "모든 디톡스 일정 조회",
            description = "데이터베이스에서 모든 디톡스 일정을 가져옵니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 목록 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeDetoxEntity.class)))
            })
    @GetMapping
    public ResponseEntity<List<TimeDetoxEntity>> getAllSchedules(@AuthenticationPrincipal UserDetails user) {
        String email = user.getUsername();
        return ResponseEntity.ok(service.getAllSchedules(email));
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
    public ResponseEntity<TimeDetoxEntity> getScheduleById(@AuthenticationPrincipal UserDetails user) {
        String email = user.getUsername();
        MemberEntity User = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        return ResponseEntity.ok(service.getScheduleById(User.getId()));
    }

    @Operation(summary = "내 시간 잠금 설정 아이템 전체 조회", description = "로그인한 사용자의 시간 잠금 설정 아이템을 전체 조회니다.")
    @GetMapping("/me")
    public ResponseEntity<?> getMyTimeDetoxSchedules(@AuthenticationPrincipal CustomUserDetails user) {

        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        List<TimeDetoxDTO> schedules = service.getAllTimeDetoxSchedulesByMember(memberId);
        return ResponseEntity.ok(schedules);
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
    public ResponseEntity<TimeDetoxEntity> updateSchedule(
            @PathVariable(name = "id") Long id,
            @RequestBody TimeDetoxEntity updatedSchedule,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        // ✅ 이벤트 날짜: 가능한 경우 updatedSchedule의 날짜를 사용, 없으면 오늘
        String dateKey = todayKey();
        // 예: updatedSchedule에 date(String yyyyMMdd)가 있다면
        if (updatedSchedule.getDate() != null && !updatedSchedule.getDate().isBlank()) {
            dateKey = updatedSchedule.getDate();
        }

        // ✅ 내 캘린더에만 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(memberId, dateKey, "Detox");

        return ResponseEntity.ok(service.updateSchedule(memberId, id, updatedSchedule));
        // ↑ (권장) updateSchedule도 memberId로 소유권 체크하도록 시그니처 변경
    }

    private String todayKey() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    @Operation(
            summary = "본인 디톡스 일정 삭제",
            description = "ID를 기준으로 본인의 디톡스 일정을 데이터베이스에서 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "일정 삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
        }

        Long memberId = user.getId();

        try {
            service.deleteSchedule(memberId, id); // 내 스케줄만 삭제, 아니면 예외

            // 오늘 날짜 "yyyyMMdd"
            String today = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 삭제 성공시에만 캘린더 업데이트
            scheduleCalendarService.addOrUpdateEvent(memberId, today, "Detox");

            return ResponseEntity.noContent().build();

        } catch (jakarta.persistence.EntityNotFoundException e) {
            // 존재하지 않거나 내 소유가 아님
            return ResponseEntity.status(404).body("삭제 실패: 스케줄이 없거나 삭제 권한이 없습니다.");
        }
    }

    @Operation(summary = "비상 탈출 문장 생성",
            description = "디톡스 비상 탈출에 필요한 문장을 생성합니다.")
    @GetMapping("/generate-phrase")
    public ResponseEntity<String> generateRandomPhrase() {

        Long memberId = getCurrentMemberIdOrNull();
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        // 서비스에서 유저별 문장 생성하도록 변경
        String phrase = service.generateRandomPhrase(memberId);

        return ResponseEntity.ok(phrase);
    }

    @Operation(summary = "비상 탈출 문장 검증",
            description = "비상 탈출 문장을 검증하고, 실행 중인 디톡스를 종료합니다.")
    @PostMapping("/verify-phrase")
    public ResponseEntity<String> verifyPhraseAndEndDetox(
            @RequestBody String inputPhrase,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        // 1) 현재 로그인한 사용자 ID
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }
        Long memberId = user.getId();

        // 2) 서비스에 memberId + 입력 문장 전달
        boolean result = service.verifyPhraseAndEndDetox(memberId, inputPhrase);

        if (result) {
            // 3) 오늘 날짜 "yyyyMMdd"
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 4) 디톡스 종료 이벤트 기록 (성공 시에만) - memberId 포함
            scheduleCalendarService.addOrUpdateEvent(memberId, today, "Detox");

            return ResponseEntity.ok("Detox has been successfully ended.");
        }

        return ResponseEntity.badRequest().body("Incorrect phrase. Detox remains active.");
    }
    /*
    @PostMapping("/app")
    public ResponseEntity<Void> addAllowedApps(@RequestBody TimeDetoxDto.App request) {
        service.addAllowedApps(request);
        return ResponseEntity.ok().build();
    }*/

    private Long getCurrentMemberIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) return null;

        // principal: CustomUserDetails
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }

        // fallback
        if (principal instanceof MemberEntity me) return me.getId();

        return null;
    }
}
