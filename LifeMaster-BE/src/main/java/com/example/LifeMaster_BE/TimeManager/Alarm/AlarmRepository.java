package com.example.LifeMaster_BE.TimeManager.Alarm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<AlarmEntity, Long> {

    Optional<AlarmEntity> findByIdAndMemberId(Long id, Long memberId);

    long countByMember_IdAndAlarmTimeBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    List<AlarmEntity> findByMember_Id(Long memberId);
}
