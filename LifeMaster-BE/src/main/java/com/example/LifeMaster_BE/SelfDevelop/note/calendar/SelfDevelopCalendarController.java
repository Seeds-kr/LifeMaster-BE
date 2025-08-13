package com.example.LifeMaster_BE.SelfDevelop.note.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // ISO 8601 형식(yyyy-MM-dd 또는 yyyy-MM-dd'T'HH:mm:ss) 아닌 경우
    // @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME
    @GetMapping
    public CalendarDailyContentDto getEvent(@RequestParam("date") LocalDate date){
        return calendarService.getEventsByDate(date);
    }

}
