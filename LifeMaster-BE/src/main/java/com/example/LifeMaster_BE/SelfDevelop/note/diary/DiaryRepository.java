package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    Optional<DiaryEntity> findByDiaryDate(LocalDate date);

    Optional<DiaryEntity> findByIdAndMember_Id(Long diaryId, Long memberId);

    Optional<DiaryEntity> findByIdAndMemberId(Long diaryId, Long memberId);

    long countByMember_IdAndDiaryDate(Long memberId, LocalDate diaryDate);
}
