package com.example.LifeMaster_BE.Challenge;

import java.time.LocalDate;

public record ChallengeProgressChangedEvent(
        Long userId,
        LocalDate date
) {
}