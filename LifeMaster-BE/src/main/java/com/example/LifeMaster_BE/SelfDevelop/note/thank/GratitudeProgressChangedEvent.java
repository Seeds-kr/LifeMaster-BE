package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import java.time.LocalDate;

public record GratitudeProgressChangedEvent(
        Long userId,
        LocalDate date
) {
}