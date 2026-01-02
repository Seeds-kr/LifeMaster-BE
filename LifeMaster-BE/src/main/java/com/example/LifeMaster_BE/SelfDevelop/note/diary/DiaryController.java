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

import java.time.LocalDate;
import java.time.ZoneId;
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
        DiaryEntity createdDiary = diaryService.createDiary(diaryDto, memberId);

        // ✅ 일기 날짜 기준으로 "내 캘린더" 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(memberId, diaryDto.getDate(), "Diary");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("diaryId", createdDiary.getId()));
    }

    @GetMapping("/{diary-id}")
    public ResponseEntity<DiaryResponse> getDiary(
            @PathVariable("diary-id") Long diaryId){
        DiaryResponse diary = diaryService.getDiary(diaryId);
        return ResponseEntity.ok(diary);
    }

    @PutMapping("/{diary-id}")
    public ResponseEntity<DiaryEntity> editDiary(
            @RequestBody UpdateDiaryDto diaryDto,
            @PathVariable("diary-id") Long diaryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        // ✅ (권장) 내 일기만 수정하도록 memberId 전달하는 형태가 안전함
        DiaryEntity updatedDiary = diaryService.updateDiary(diaryId, diaryDto, memberId);
        // 만약 서비스 시그니처를 못 바꾸면, updateDiary 내부에서 소유권 체크가 반드시 필요

        // ✅ 수정된 일기 날짜 기준으로 "내 캘린더" 이벤트 추가
        scheduleCalendarService.addOrUpdateEvent(memberId, diaryDto.getDate(), "Diary");

        return ResponseEntity.ok(updatedDiary);
    }

    @DeleteMapping("/{diary-id}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable("diary-id") Long diaryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        // 1) 삭제 전: 다이어리 조회해서 날짜 확보(본인 것만)
        DiaryEntity diary = diaryService.getDiaryByIdAndMemberId(diaryId, memberId);

        String dateKey = diary.getDiaryDate()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2) 다이어리 삭제
        diaryService.deleteDiary(diaryId, memberId);

        // 3) 같은 memberId + 같은 날짜의 다이어리가 0개면 캘린더 이벤트 제거
        if (diaryService.countDiaryByMemberIdAndDate(memberId, diary.getDiaryDate()) == 0) {
            scheduleCalendarService.deleteSpecificEvent(memberId, dateKey, "Diary");
        }

        return ResponseEntity.noContent().build();
    }

}
