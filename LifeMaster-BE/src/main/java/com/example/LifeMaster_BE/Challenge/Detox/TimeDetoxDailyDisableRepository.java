package com.example.LifeMaster_BE.Challenge.Detox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface    TimeDetoxDailyDisableRepository extends JpaRepository<TimeDetoxDailyDisableEntity, Long> {

    boolean existsByMember_IdAndTimeDetox_IdAndDisabledDate(
            Long memberId,
            Long timeDetoxId,
            LocalDate disabledDate
    );

    List<TimeDetoxDailyDisableEntity> findAllByMember_IdAndDisabledDateAndTimeDetox_IdIn(
            Long memberId,
            LocalDate disabledDate,
            Collection<Long> timeDetoxIds
    );

    List<TimeDetoxDailyDisableEntity>
    findByMember_IdAndDisabledDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<TimeDetoxDailyDisableEntity>
    findByMember_IdAndTimeDetox_IdAndDisabledDate(
            Long memberId,
            Long timeDetoxId,
            LocalDate disabledDate
    );

    void deleteAllByMember_IdAndTimeDetox_Id(Long memberId, Long timeDetoxId);
}