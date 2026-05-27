package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.CreateThankDto;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.ThankResponse;
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
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        ThankEntity createdThank = thankService.createThank(thankDto, memberId);

        // ✅ 오류 해결: memberId 추가
        scheduleCalendarService.addOrUpdateEvent(memberId, thankDto.getDate(), "Thank");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("thankId", createdThank.getId()));
    }

    @GetMapping("/{thank-id}")
    public ResponseEntity<ThankResponse> getThank(
            @PathVariable("thank-id") Long thankId,
            @AuthenticationPrincipal CustomUserDetails user){
        Long memberId = user.getId();
        ThankResponse thank = thankService.getThank(thankId, memberId);
        return ResponseEntity.ok(thank);
    }

    @PutMapping("/{thank-id}")
    public ResponseEntity<ThankEntity> editThank(
            @RequestBody UpdateThankDto thankDto,
            @PathVariable("thank-id") Long thankId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = user.getId();

        ThankEntity updatedThank = thankService.updateThank(thankId, thankDto, memberId);

        // Thank의 실제 날짜 기준 (String yyyyMMdd)
        String dateKey = updatedThank.getThankDate()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // ✅ 오류 해결: memberId 추가
        scheduleCalendarService.addOrUpdateEvent(memberId, dateKey, "Thank");

        return ResponseEntity.ok(updatedThank);
    }

    @DeleteMapping("/{thank-id}")
    public ResponseEntity<Void> deleteThank(
            @PathVariable("thank-id") Long thankId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = user.getId();

        // 1️⃣ 삭제 전: Thank 조회 (본인 것만)
        ThankEntity thank = thankService.getThankByIdAndMemberId(thankId, memberId);

        // 날짜(String yyyyMMdd)
        String dateKey = thank.getThankDate()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2️⃣ Thank 삭제
        thankService.deleteThank(thankId, memberId);

        // 3️⃣ 같은 memberId + 같은 날짜에 Thank가 0개면 캘린더 이벤트 제거
        if (thankService.countThankByMemberIdAndDate(memberId, thank.getThankDate()) == 0) {
            scheduleCalendarService.deleteSpecificEvent(memberId, dateKey, "Thank");
        }

        return ResponseEntity.noContent().build();
    }
}
