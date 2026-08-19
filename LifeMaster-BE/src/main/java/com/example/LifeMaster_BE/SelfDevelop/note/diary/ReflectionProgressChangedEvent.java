package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import java.time.LocalDate;

public record ReflectionProgressChangedEvent(
        Long userId,
        LocalDate date
) {
}