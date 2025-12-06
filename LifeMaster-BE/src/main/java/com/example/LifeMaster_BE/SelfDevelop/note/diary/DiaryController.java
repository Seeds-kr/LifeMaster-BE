package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.CreateDiaryDto;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.UpdateDiaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/schedule/self-reflection/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final ScheduleCalendarService scheduleCalendarService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> newDiary(
            @RequestBody CreateDiaryDto diaryDto,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long memberId = user.getId();
        DiaryEntity createdDiary = diaryService.createDiary(diaryDto, memberId);

        // 오늘 날짜 "yyyyMMdd"로 변환
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(today, "Diary");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("diartId", createdDiary.getId()));
    }

    @PutMapping("/{diary-id}")
    public ResponseEntity<DiaryEntity> editDiary(
            @RequestBody UpdateDiaryDto diaryDto,
            @PathVariable("diary-id") Long diaryId) {

        DiaryEntity updatedDiary = diaryService.updateDiary(diaryId, diaryDto);

        // 오늘 날짜 "yyyyMMdd"로 변환
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(today, "Diary");

        return ResponseEntity.ok(updatedDiary);
    }

    @DeleteMapping("/{diary-id}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable("diary-id") Long diaryId){

        diaryService.deleteDiary(diaryId);
        return ResponseEntity.noContent().build();
    }
}
