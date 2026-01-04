package com.example.LifeMaster_BE.FunctionManager.Calender;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Schedule Calendar API", description = "캘린더 관리 API")
@RestController("scheduleCalendarController")
@RequestMapping("/calendar")
public class ScheduleCalendarController {

    private final ScheduleCalendarService calendarService;

    public ScheduleCalendarController(ScheduleCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    private ScheduleCalendarResponseDto toResponse(ScheduleCalendarEntity entity) {
        return new ScheduleCalendarResponseDto(
                entity.getId(),
                entity.getDate(),
                entity.getDay(),
                entity.getEvents()
        );
    }

    /**
     * ✅ 여기만 프로젝트에 맞게 바꾸면 됨.
     * - 지금은 "username에 memberId가 들어있다"는 가정으로 Long 변환.
     * - 실제로는 CustomUserDetails.getMemberId() 같은 방식일 확률이 높음.
     */
    private Long getMemberId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        if (userDetails instanceof CustomUserDetails customUser) {
            return customUser.getId();
        }

        throw new RuntimeException(
                "지원하지 않는 UserDetails 타입입니다: " + userDetails.getClass().getName()
        );
    }

    @Operation(summary = "전체 조회", description = "현재 로그인한 유저의 캘린더 엔트리를 모두 조회합니다.")
    @GetMapping
    public List<ScheduleCalendarResponseDto> getAllEntries(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long memberId = getMemberId(userDetails);

        return calendarService.findAll(memberId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "특정 멤버 전체 조회", description = "입력받은 memberId의 캘린더 엔트리를 모두 조회합니다.")
    @GetMapping("/member/{memberId}")
    public List<ScheduleCalendarResponseDto> getAllEntriesByMemberId(
            @PathVariable(name = "memberId") Long memberId
    ) {
        return calendarService.findAll(memberId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "특정 날짜 조회", description = "현재 로그인한 유저의 입력한 날짜(YYYYMMDD) 엔트리를 조회합니다.")
    @GetMapping("/{date}")
    public ResponseEntity<ScheduleCalendarResponseDto> getEntriesByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "date") String date
    ) {
        Long memberId = getMemberId(userDetails);

        Optional<ScheduleCalendarEntity> entity = calendarService.findByDate(memberId, date);
        return entity.map(e -> ResponseEntity.ok(toResponse(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "월별 조회", description = "현재 로그인한 유저의 입력한 월(YYYYMM) 엔트리를 조회합니다.")
    @GetMapping("/month/{date}")
    public List<ScheduleCalendarResponseDto> getEntriesByMonth(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "date") String month
    ) {
        Long memberId = getMemberId(userDetails);

        return calendarService.findByMonth(memberId, month).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "새로운 날짜에 이벤트 생성",
            description = "현재 로그인한 유저의 입력한 날짜(YYYYMMDD)에 새로운 이벤트 리스트를 생성합니다.")
    @PostMapping("/create/{date}/events")
    public ResponseEntity<ScheduleCalendarResponseDto> createEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "date") String date,
            @RequestBody List<String> events
    ) {
        Long memberId = getMemberId(userDetails);

        ScheduleCalendarEntity entry = calendarService.createEvent(memberId, date, events);
        return ResponseEntity.ok(toResponse(entry));
    }

    @Operation(summary = "캘린더 엔티티 생성",
            description = "현재 로그인한 유저의 캘린더 엔티티를 생성합니다. 날짜 형식은 YYYYMMDD이며, TODO 리스트는 기본값으로 NULL로 설정됩니다.")
    @PostMapping("/create")
    public ResponseEntity<ScheduleCalendarResponseDto> createDay(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("date") String date
    ) {
        Long memberId = getMemberId(userDetails);

        ScheduleCalendarEntity calendarEntity = new ScheduleCalendarEntity();
        calendarEntity.setDate(date);

        ScheduleCalendarEntity entry = calendarService.createCalendarEntity(memberId, calendarEntity);
        return ResponseEntity.ok(toResponse(entry));
    }

    @Operation(summary = "특정 날짜에 항목 추가",
            description = "현재 로그인한 유저의 입력한 날짜(YYYYMMDD)에 이벤트를 추가하거나 기존 이벤트를 수정합니다.")
    @PostMapping("/{date}/add")
    public ResponseEntity<ScheduleCalendarResponseDto> addEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "date") String date,
            @RequestBody EventRequestDto request
    ) {
        Long memberId = getMemberId(userDetails);

        ScheduleCalendarEntity entry = calendarService.addOrUpdateEvent(memberId, date, request.getEvent());
        return ResponseEntity.ok(toResponse(entry));
    }

    @Operation(summary = "특정 날짜의 모든 항목 삭제",
            description = "현재 로그인한 유저의 입력한 날짜(YYYYMMDD) 엔트리를 삭제합니다.")
    @DeleteMapping("/{date}")
    public ResponseEntity<String> deleteEventByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "date") String date
    ) {
        Long memberId = getMemberId(userDetails);

        boolean deleted = calendarService.deleteEventByDate(memberId, date);
        if (deleted) {
            return ResponseEntity.ok("삭제되었습니다.");
        } else {
            return ResponseEntity.status(404).body("해당 날짜에 항목이 없습니다.");
        }
    }

    @Operation(summary = "특정 날짜의 특정 항목 삭제",
            description = "현재 로그인한 유저의 입력한 날짜(YYYYMMDD)와 이벤트 내용을 기반으로 특정 이벤트를 삭제합니다.")
    @DeleteMapping("/{date}/event")
    public ResponseEntity<ScheduleCalendarResponseDto> deleteSpecificEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "date") String date,
            @RequestBody EventRequestDto request
    ) {
        Long memberId = getMemberId(userDetails);

        ScheduleCalendarEntity entry = calendarService.deleteSpecificEvent(memberId, date, request.getEvent());
        if (entry != null) {
            return ResponseEntity.ok(toResponse(entry));
        } else {
            return ResponseEntity.status(404).build();
        }
    }
}
