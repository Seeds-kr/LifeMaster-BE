package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    DiaryEntity findByDiaryDate(LocalDate date);

}
