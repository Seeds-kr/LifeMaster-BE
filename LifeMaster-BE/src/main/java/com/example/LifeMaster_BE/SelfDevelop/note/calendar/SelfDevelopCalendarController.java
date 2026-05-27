package com.example.LifeMaster_BE.SelfDevelop.note.calendar;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @RequestParam("date") LocalDate date,
            @AuthenticationPrincipal CustomUserDetails user){
        Long memberId = user.getId();
        CalendarDailyContentDto response = calendarService.getEventsByDate(date, memberId);
        return ResponseEntity.ok(response);
    }

}
