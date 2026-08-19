package com.example.LifeMaster_BE.TimeManager.Sleep;

import java.time.LocalDate;

public record SleepRecordedEvent(
        Long userId,
        LocalDate sleepDate
) {
}