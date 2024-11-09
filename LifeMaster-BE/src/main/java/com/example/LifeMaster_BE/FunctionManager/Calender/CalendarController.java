package com.example.LifeMaster_BE.FunctionManager.Calender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    // 전체 조회
    @GetMapping
    public List<CalendarEntity> getAllEntries() {
        return calendarService.findAll();
    }

    // 특정 날짜 조회
    @GetMapping("/{date}")
    public List<CalendarEntity> getEntriesByDate(@PathVariable(name="date") String date) {
        return calendarService.findByDate(date);
    }

    // 월별 조회
    @GetMapping("/month/{date}")
    public List<CalendarEntity> getEntriesByMonth(@PathVariable(name="date") String month) {
        return calendarService.findByMonth(month);
    }

    // 새로운 날짜에 이벤트 생성
    @PostMapping("/create/{date}/events")
    public ResponseEntity<CalendarEntity> createEvent(@PathVariable(name="date") String date, @RequestBody List<String> events) {
        CalendarEntity entry = calendarService.createEvent(date,events);
        return ResponseEntity.ok(entry);
    }

    @PostMapping("/create")
    public ResponseEntity<CalendarEntity> createEvent(@RequestBody CalendarEntity calendarEntity) {
        CalendarEntity entry = calendarService.createEvent(calendarEntity.getDate(),calendarEntity.getEvents());
        return ResponseEntity.ok(entry);
    }

    // 특정 날짜에 항목 추가
    @PostMapping("/{date}/add")
    public ResponseEntity<CalendarEntity> addEvent(@PathVariable(name="date") String date, @RequestBody String event) {
        CalendarEntity entry = calendarService.addOrUpdateEvent(date, event);
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

    // 특정 날짜의 특정 항목 삭제
    @DeleteMapping("/{date}/event")
    public ResponseEntity<CalendarEntity> deleteSpecificEvent(@PathVariable(name="date") String date, @RequestBody String event) {
        CalendarEntity entry = calendarService.deleteSpecificEvent(date, event);
        if (entry != null) {
            return ResponseEntity.ok(entry);
        } else {
            return ResponseEntity.status(404).build();
        }
    }
}

