package com.example.LifeMaster_BE.FunctionManager.Calender;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleCalendarRepository extends JpaRepository<ScheduleCalendarEntity, Long> {
    Optional<ScheduleCalendarEntity> findByDate(String date);
    List<ScheduleCalendarEntity> findByDateStartingWith(String month);

    Optional<ScheduleCalendarEntity> findByMemberIdAndDate(Long memberId, String date);
    List<ScheduleCalendarEntity> findByMemberIdAndDateStartingWith(Long memberId, String yyyymm);
    List<ScheduleCalendarEntity> findAllByMemberId(Long memberId);
}
