package com.example.LifeMaster_BE.FunctionManager.Calender;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
    List<CalendarEntity> findByDate(String date);
    List<CalendarEntity> findByDateStartingWith(String month);
}

