package com.example.LifeMaster_BE.SelfDevelop.note.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController("selfDevelopCalendarController")
@RequestMapping("/schedule/self-reflection")
@RequiredArgsConstructor
public class SelfDevelopCalendarController {

    private final SelfDevelopCalendarService calendarService;

    @GetMapping
    public ResponseEntity<CalendarDailyContentDto> getEvent(
            @RequestParam("date") LocalDate date){

        CalendarDailyContentDto response = calendarService.getEventsByDate(date);
        return ResponseEntity.ok(response);
    }

}
