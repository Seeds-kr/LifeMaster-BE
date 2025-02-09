package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final MemberRepository memberRepository;

    public DiaryEntity createDiary(DiaryEntity diary, Long memberId){
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        member.addDiary(diary);
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
