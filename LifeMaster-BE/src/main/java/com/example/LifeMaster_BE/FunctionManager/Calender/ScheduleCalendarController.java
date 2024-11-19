package com.example.LifeMaster_BE.FunctionManager.Calender;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("scheduleCalendarController")
@RequestMapping("/calendar")
public class ScheduleCalendarController {

    private final ScheduleCalendarService calendarService;

    public ScheduleCalendarController(ScheduleCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    // 전체 조회
    @Operation(summary = "전체 조회", description = "캘린더 전체 리스트 조회")
    @GetMapping
    public List<ScheduleCalendarEntity> getAllEntries() {
        return calendarService.findAll();
    }

    // 특정 날짜 조회
    @GetMapping("/{date}")
    public List<ScheduleCalendarEntity> getEntriesByDate(@PathVariable(name="date") String date) {
        return calendarService.findByDate(date);
    }

    // 월별 조회
    @GetMapping("/month/{date}")
    public List<ScheduleCalendarEntity> getEntriesByMonth(@PathVariable(name="date") String month) {
        return calendarService.findByMonth(month);
    }

    // 새로운 날짜에 이벤트 생성
    @PostMapping("/create/{date}/events")
    public ResponseEntity<ScheduleCalendarEntity> createEvent(@PathVariable(name="date") String date, @RequestBody List<String> events) {
        ScheduleCalendarEntity entry = calendarService.createEvent(date,events);
        return ResponseEntity.ok(entry);
    }

    @PostMapping("/create")
    @Operation(summary = "캘린더 엔티티 생성", description = "캘린더 엔티티 생성, 날짜 형식 YYYYMMDD, TODO 리스트 기본 NULL")
    public ResponseEntity<ScheduleCalendarEntity> createDay(@RequestBody ScheduleCalendarEntity calendarEntity) {
        ScheduleCalendarEntity entry = calendarService.createCalendarEntity(calendarEntity);
        return ResponseEntity.ok(entry);
    }

    // 특정 날짜에 항목 추가
    @PostMapping("/{date}/add")
    public ResponseEntity<ScheduleCalendarEntity> addEvent(@PathVariable(name="date") String date, @RequestBody String event) {
        ScheduleCalendarEntity entry = calendarService.addOrUpdateEvent(date, event);
        return ResponseEntity.ok(entry);
    }

    // 특정 날짜 전체 삭제
    @DeleteMapping("/{date}")
    public ResponseEntity<String> deleteEventByDate(@PathVariable(name="date") String date) {
        boolean deleted = calendarService.deleteEventByDate(date);
        if (deleted) {
            return ResponseEntity.ok("삭제되었습니다.");
        } else {
            return ResponseEntity.status(404).body("해당 날짜에 항목이 없습니다.");
        }
    }

    // 특정 날짜의 특정 갓생 항목 삭제
    @DeleteMapping("/{date}/event")
    public ResponseEntity<ScheduleCalendarEntity> deleteSpecificEvent(@PathVariable(name="date") String date, @RequestBody String event) {
        ScheduleCalendarEntity entry = calendarService.deleteSpecificEvent(date, event);
        if (entry != null) {
            return ResponseEntity.ok(entry);
        } else {
            return ResponseEntity.status(404).build();
        }
    }
}

