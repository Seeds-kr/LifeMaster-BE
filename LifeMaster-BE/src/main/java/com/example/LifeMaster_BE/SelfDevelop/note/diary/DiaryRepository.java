package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    DiaryEntity findByDiaryDate(LocalDateTime diaryDate);

}
