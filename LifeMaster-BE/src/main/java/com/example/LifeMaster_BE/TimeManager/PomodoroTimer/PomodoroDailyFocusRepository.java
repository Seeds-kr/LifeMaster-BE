package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PomodoroDailyFocusRepository extends JpaRepository<PomodoroDailyFocusEntity, Long> {

    Optional<PomodoroDailyFocusEntity> findByMember_IdAndDate(Long memberId, String date);

    List<PomodoroDailyFocusEntity> findByMember_IdAndDateBetweenOrderByDateAsc(
            Long memberId,
            String startDate,
            String endDate
    );
}