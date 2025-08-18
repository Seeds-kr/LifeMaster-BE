package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    Optional<DiaryEntity> findByDiaryDate(LocalDate date);
}
