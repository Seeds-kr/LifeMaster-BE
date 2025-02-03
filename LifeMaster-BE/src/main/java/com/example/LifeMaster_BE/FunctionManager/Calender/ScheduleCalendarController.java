package com.example.LifeMaster_BE.FunctionManager.Calender;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Schedule Calendar API", description = "캘린더 관리 API")
@RestController("scheduleCalendarController")
@RequestMapping("/calendar")
public class ScheduleCalendarController {

    private final ScheduleCalendarService calendarService;

    public ScheduleCalendarController(ScheduleCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Operation(summary = "전체 조회", description = "캘린더에 저장된 모든 엔트리를 조회합니다.")
    @GetMapping
    public List<ScheduleCalendarEntity> getAllEntries() {
        return calendarService.findAll();
    }

    @Operation(summary = "특정 날짜 조회", description = "입력한 날짜(YYYYMMDD)에 저장된 엔트리를 조회합니다.")
    @GetMapping("/{date}")
    public Optional<ScheduleCalendarEntity> getEntriesByDate(@PathVariable(name = "date") String date) {
        return calendarService.findByDate(date);
    }

    @Operation(summary = "월별 조회", description = "입력한 월(YYYYMM)에 저장된 엔트리를 조회합니다.")
    @GetMapping("/month/{date}")
    public List<ScheduleCalendarEntity> getEntriesByMonth(@PathVariable(name = "date") String month) {
        return calendarService.findByMonth(month);
    }

    @Operation(summary = "새로운 날짜에 이벤트 생성",
            description = "입력한 날짜(YYYYMMDD)에 새로운 이벤트 리스트를 생성합니다.")
    @PostMapping("/create/{date}/events")
    public ResponseEntity<ScheduleCalendarEntity> createEvent(
            @PathVariable(name = "date") String date,
            @RequestBody List<String> events) {
        ScheduleCalendarEntity entry = calendarService.createEvent(date, events);
        return ResponseEntity.ok(entry);
    }

    @Operation(
            summary = "캘린더 엔티티 생성",
            description = "캘린더 엔티티를 생성합니다. 날짜 형식은 YYYYMMDD이며, TODO 리스트는 기본값으로 NULL로 설정됩니다."
    )
    @PostMapping("/create")
    public ResponseEntity<ScheduleCalendarEntity> createDay(@RequestParam("date") String date) {
        ScheduleCalendarEntity calendarEntity = new ScheduleCalendarEntity();
        calendarEntity.setDate(date); // 날짜만 설정
        ScheduleCalendarEntity entry = calendarService.createCalendarEntity(calendarEntity);
        return ResponseEntity.ok(entry);
    }

    @Operation(summary = "특정 날짜에 항목 추가",
            description = "입력한 날짜(YYYYMMDD)에 이벤트를 추가하거나 기존 이벤트를 수정합니다.")
    @PostMapping("/{date}/add")
    public ResponseEntity<ScheduleCalendarEntity> addEvent(
            @PathVariable(name = "date") String date,
            @RequestBody String event) {
        ScheduleCalendarEntity entry = calendarService.addOrUpdateEvent(date, event);
        return ResponseEntity.ok(entry);
    }

    @Operation(summary = "특정 날짜의 모든 항목 삭제",
            description = "입력한 날짜(YYYYMMDD)의 모든 엔트리를 삭제합니다.")
    @DeleteMapping("/{date}")
    public ResponseEntity<String> deleteEventByDate(@PathVariable(name = "date") String date) {
        boolean deleted = calendarService.deleteEventByDate(date);
        if (deleted) {
            return ResponseEntity.ok("삭제되었습니다.");
        } else {
            return ResponseEntity.status(404).body("해당 날짜에 항목이 없습니다.");
        }
    }

    @Operation(summary = "특정 날짜의 특정 항목 삭제",
            description = "입력한 날짜(YYYYMMDD)와 이벤트 내용을 기반으로 특정 엔트리를 삭제합니다.")
    @DeleteMapping("/{date}/event")
    public ResponseEntity<ScheduleCalendarEntity> deleteSpecificEvent(
            @PathVariable(name = "date") String date,
            @RequestBody String event) {
        ScheduleCalendarEntity entry = calendarService.deleteSpecificEvent(date, event);
        if (entry != null) {
            return ResponseEntity.ok(entry);
        } else {
            return ResponseEntity.status(404).build();
        }
    }
}
