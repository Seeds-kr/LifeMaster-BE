package com.example.LifeMaster_BE.Challenge.Detox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class TimeDetoxService {

    @Autowired
    private TimeDetoxRepository repository;

    public TimeDetoxEntity createSchedule(TimeDetoxEntity schedule) {
        return repository.save(schedule);
    }

    public List<TimeDetoxEntity> getAllSchedules() {
        return repository.findAll();
    }

    public TimeDetoxEntity getScheduleById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public TimeDetoxEntity updateSchedule(Long id, TimeDetoxEntity updatedSchedule) {
        TimeDetoxEntity schedule = getScheduleById(id);
        schedule.setCycle(updatedSchedule.getCycle());
        schedule.setDay(updatedSchedule.getDay());
        schedule.setStartTime(updatedSchedule.getStartTime());
        schedule.setEndTime(updatedSchedule.getEndTime());
        schedule.setActive(updatedSchedule.isActive());
        return repository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        repository.deleteById(id);
    }

    public boolean isAppLocked(String day, LocalTime currentTime) {
        List<TimeDetoxEntity> activeSchedules = repository.findByIsActiveTrue();
        return activeSchedules.stream().anyMatch(schedule ->
                schedule.getDay().equalsIgnoreCase(day) &&
                        !currentTime.isBefore(schedule.getStartTime()) &&
                        !currentTime.isAfter(schedule.getEndTime())
        );
    }
}