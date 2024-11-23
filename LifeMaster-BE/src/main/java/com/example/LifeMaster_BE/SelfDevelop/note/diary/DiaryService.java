package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public DiaryEntity createDiary(DiaryEntity diary){
        return diaryRepository.save(diary);
    }

    public DiaryEntity editDiary(Long diaryId, DiaryUpdateDto diaryDto){
        DiaryEntity diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Diary not found"));

        diary.setDiaryContent(diaryDto.getDiaryContent());
        return diary;
    }

    public void deleteDiary(Long diaryId){
        diaryRepository.deleteById(diaryId);
    }
}
