package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import java.time.LocalDate;

public record PomodoroCompletedEvent(
        Long userId,
        LocalDate date
) {
}