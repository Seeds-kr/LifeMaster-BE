package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.CreateThankDto;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.UpdateThankDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/schedule/self-reflection/thank")
@RequiredArgsConstructor
public class ThankController {

    private final ThankService thankService;
    private final ScheduleCalendarService scheduleCalendarService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> newThank(
            @RequestBody CreateThankDto thankDto,
            @AuthenticationPrincipal CustomUserDetails user){
        Long memberId = user.getId();
        ThankEntity createdThank = thankService.createThank(thankDto, memberId);

        // 오늘 날짜 "yyyyMMdd"로 변환
        String today = LocalDate
                .now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(today, "Thank");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("thankId", createdThank.getId()));
    }

    @PutMapping("/{thank-id}")
    public ResponseEntity<ThankEntity> editThank(
            @RequestBody UpdateThankDto thankDto,
            @PathVariable("thank-id") Long thankId){
        ThankEntity updatedThank = thankService.updateThank(thankId, thankDto);

        // 오늘 날짜 "yyyyMMdd"로 변환
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(today, "Thank");

        return ResponseEntity.ok(updatedThank);
    }

    @DeleteMapping("/{thank-id}")
    public ResponseEntity<Void> deleteThank(
            @PathVariable("thank-id") Long thankId){
        thankService.deleteThank(thankId);
        return ResponseEntity.noContent().build();
    }
}
