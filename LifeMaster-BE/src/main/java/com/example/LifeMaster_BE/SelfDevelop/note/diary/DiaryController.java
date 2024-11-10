package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/schedule/self-reflection/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * DiaryEntity 관련 매서드
     * User id 처리가 필요(session or client 측에서)
     */

    @PostMapping()
    public ResponseEntity<DiaryEntity> newDiary(@RequestBody DiaryEntity diary) {
        DiaryEntity createdDiary = diaryService.createDiary(diary);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDiary);
    }

    @PatchMapping("/{diary-id}")
    public ResponseEntity<DiaryEntity> editDiary(
            @RequestBody DiaryUpdateDto diaryDto,
            @PathVariable("diary-id") Long diaryId) {

        DiaryEntity updatedDiary = diaryService.editDiary(diaryId, diaryDto);
        return ResponseEntity.ok(updatedDiary);
    }

    @DeleteMapping("/{diary-id}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable("diary-id") Long diaryId){
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.noContent().build();
    }
}
