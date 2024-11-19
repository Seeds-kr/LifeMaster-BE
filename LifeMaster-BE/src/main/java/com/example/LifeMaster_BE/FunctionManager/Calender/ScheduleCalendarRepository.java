package com.example.LifeMaster_BE.FunctionManager.Calender;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScheduleCalendarRepository extends JpaRepository<ScheduleCalendarEntity, Long> {
    List<ScheduleCalendarEntity> findByDate(String date);
    List<ScheduleCalendarEntity> findByDateStartingWith(String month);
}

