package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.CreateDiaryDto;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.UpdateDiaryDto;
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

    public DiaryEntity createDiary(CreateDiaryDto diaryDto, Long memberId){

        DiaryEntity newDiary = diaryDto.toEntity();
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        member.addDiary(newDiary);
        return diaryRepository.save(newDiary);
    }

    public DiaryEntity updateDiary(Long diaryId, UpdateDiaryDto diaryDto){
        DiaryEntity diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException("Diary not found"));

        diary.setDiaryContent(diaryDto.getDiaryContent());
        return diary;
    }

    public void deleteDiary(Long diaryId){
        diaryRepository.deleteById(diaryId);
    }
}
