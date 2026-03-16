package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.CreateDiaryDto;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.DiaryResponse;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.UpdateDiaryDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public DiaryEntity createDiary(CreateDiaryDto diaryDto, Long memberId){

        DiaryEntity newDiary1 = diaryDto.toEntity();
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        DiaryEntity newDiary = newDiary1.toBuilder() .member(member) .build();
        return diaryRepository.save(newDiary);
    }

    public DiaryResponse getDiary(Long diaryId){
        DiaryEntity diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException("Diary not found"));

        return DiaryResponse.builder()
                .diaryContent(diary.getDiaryContent())
                .diaryDate(diary.getDiaryDate())
                .build();
    }

    public DiaryEntity updateDiary(Long diaryId, UpdateDiaryDto diaryDto, Long memberId) {
        DiaryEntity diary = diaryRepository
                .findByIdAndMemberId(diaryId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Diary not found or no permission"));

        diary.setDiaryContent(diaryDto.getDiaryContent());
        return diary;
    }

    public DiaryEntity getDiaryByIdAndMemberId(Long diaryId, Long memberId) {
        return diaryRepository.findByIdAndMember_Id(diaryId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Diary not found"));
    }

    public void deleteDiary(Long diaryId, Long memberId) {
        DiaryEntity diary = getDiaryByIdAndMemberId(diaryId, memberId);
        diaryRepository.delete(diary);
    }

    public long countDiaryByMemberIdAndDate(Long memberId, LocalDate diaryDate) {
        return diaryRepository.countByMember_IdAndDiaryDate(memberId, diaryDate);
    }

    public void deleteDiary(Long diaryId){
        diaryRepository.deleteById(diaryId);
    }
}
