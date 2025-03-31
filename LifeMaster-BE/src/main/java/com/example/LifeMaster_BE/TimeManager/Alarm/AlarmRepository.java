package com.example.LifeMaster_BE.TimeManager.Alarm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlarmRepository extends JpaRepository<AlarmEntity, Long> {

    Optional<AlarmEntity> findByIdAndMemberId(Long id, Long memberId);
}
