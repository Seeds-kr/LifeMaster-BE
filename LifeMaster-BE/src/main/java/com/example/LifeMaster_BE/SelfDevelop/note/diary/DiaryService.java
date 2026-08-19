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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DiaryEntity createDiary(CreateDiaryDto diaryDto, Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        DiaryEntity newDiary = diaryDto.toEntity()
                .toBuilder()
                .member(member)
                .build();

        if (diaryRepository.findByDiaryDateAndMember_Id(
                newDiary.getDiaryDate(),
                memberId
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "해당 날짜에는 이미 일기가 작성되어 있습니다."
            );
        }

        DiaryEntity saved = diaryRepository.save(newDiary);

        eventPublisher.publishEvent(
                new ReflectionProgressChangedEvent(
                        memberId,
                        saved.getDiaryDate()
                )
        );

        return saved;
    }

    public DiaryResponse getDiary(Long diaryId, Long memberId) {
        DiaryEntity diary = diaryRepository.findByIdAndMember_Id(diaryId, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Diary not found or no permission"));

        return DiaryResponse.builder()
                .diaryContent(diary.getDiaryContent())
                .diaryDate(diary.getDiaryDate())
                .build();
    }

    public DiaryEntity updateDiary(
            Long diaryId,
            UpdateDiaryDto diaryDto,
            Long memberId
    ) {
        DiaryEntity diary = diaryRepository.findByIdAndMemberId(diaryId, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Diary not found or no permission"));

        diary.setDiaryContent(diaryDto.getDiaryContent());

        return diary;
    }

    public DiaryEntity getDiaryByIdAndMemberId(Long diaryId, Long memberId) {
        return diaryRepository.findByIdAndMember_Id(diaryId, memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Diary not found"));
    }

    public void deleteDiary(Long diaryId, Long memberId) {
        DiaryEntity diary = getDiaryByIdAndMemberId(diaryId, memberId);
        LocalDate diaryDate = diary.getDiaryDate();

        diaryRepository.delete(diary);
        diaryRepository.flush();

        eventPublisher.publishEvent(
                new ReflectionProgressChangedEvent(
                        memberId,
                        diaryDate
                )
        );
    }

    public long countDiaryByMemberIdAndDate(
            Long memberId,
            LocalDate diaryDate
    ) {
        return diaryRepository.countByMember_IdAndDiaryDate(
                memberId,
                diaryDate
        );
    }
}