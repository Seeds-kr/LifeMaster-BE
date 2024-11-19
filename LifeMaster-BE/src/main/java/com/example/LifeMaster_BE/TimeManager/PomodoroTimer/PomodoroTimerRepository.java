package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PomodoroTimerRepository extends JpaRepository<PomodoroTimerEntity, Long> {
    List<PomodoroTimerEntity> findByDate(String date);
    @Transactional
    void deleteAllByDate(String date);
}

