package com.example.LifeMaster_BE.Challenge.Detox;

import java.time.LocalDate;

public record DetoxProgressChangedEvent(
        Long userId,
        LocalDate date
) {
}