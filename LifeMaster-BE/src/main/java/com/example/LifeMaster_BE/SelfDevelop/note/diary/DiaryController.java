package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.CreateDiaryDto;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.DiaryResponse;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.UpdateDiaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        DiaryEntity createdDiary =
                diaryService.createDiary(diaryDto, memberId);

        String dateKey = createdDiary.getDiaryDate()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        scheduleCalendarService.addOrUpdateEvent(
                memberId,
                dateKey,
                "Diary"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("diaryId", createdDiary.getId()));
    }

    @GetMapping("/{diary-id}")
    public ResponseEntity<DiaryResponse> getDiary(
            @PathVariable("diary-id") Long diaryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        DiaryResponse diary =
                diaryService.getDiary(diaryId, memberId);

        return ResponseEntity.ok(diary);
    }

    @PutMapping("/{diary-id}")
    public ResponseEntity<DiaryEntity> editDiary(
            @RequestBody UpdateDiaryDto diaryDto,
            @PathVariable("diary-id") Long diaryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        DiaryEntity updatedDiary = diaryService.updateDiary(
                diaryId,
                diaryDto,
                memberId
        );

        String dateKey = updatedDiary.getDiaryDate()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        scheduleCalendarService.addOrUpdateEvent(
                memberId,
                dateKey,
                "Diary"
        );

        return ResponseEntity.ok(updatedDiary);
    }

    @DeleteMapping("/{diary-id}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable("diary-id") Long diaryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        DiaryEntity diary =
                diaryService.getDiaryByIdAndMemberId(diaryId, memberId);

        String dateKey = diary.getDiaryDate()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        diaryService.deleteDiary(diaryId, memberId);

        if (diaryService.countDiaryByMemberIdAndDate(
                memberId,
                diary.getDiaryDate()
        ) == 0) {
            scheduleCalendarService.deleteSpecificEvent(
                    memberId,
                    dateKey,
                    "Diary"
            );
        }

        return ResponseEntity.noContent().build();
    }
}