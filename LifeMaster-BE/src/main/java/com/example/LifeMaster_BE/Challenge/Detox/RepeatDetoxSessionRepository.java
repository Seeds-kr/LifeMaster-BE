package com.example.LifeMaster_BE.Challenge.Detox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepeatDetoxSessionRepository extends JpaRepository<RepeatDetoxSession, Long> {

    Optional<RepeatDetoxSession> findFirstByRepeatDetox_IdAndEndedAtIsNullOrderByStartedAtDesc(
            Long repeatDetoxId
    );

    boolean existsByRepeatDetox_IdAndEndedAtIsNull(Long repeatDetoxId);

    List<RepeatDetoxSession> findByMember_IdAndDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<RepeatDetoxSession> findByRepeatDetox_Id(Long repeatDetoxId);
}