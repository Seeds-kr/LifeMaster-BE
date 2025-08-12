package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/schedule/self-reflection/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<DiaryEntity> newDiary(
            @RequestBody DiaryEntity diary,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long memberId = user.getId();
        DiaryEntity createdDiary = diaryService.createDiary(diary, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDiary);
    }

    @PatchMapping("/{diary-id}")
    public ResponseEntity<DiaryEntity> editDiary(
            @RequestBody DiaryUpdateDto diaryDto,
            @PathVariable("diary-id") Long diaryId) {

        DiaryEntity updatedDiary = diaryService.updateDiary(diaryId, diaryDto);
        return ResponseEntity.ok(updatedDiary);
    }

    @DeleteMapping("/{diary-id}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable("diary-id") Long diaryId){

        diaryService.deleteDiary(diaryId);
        return ResponseEntity.noContent().build();
    }
}
