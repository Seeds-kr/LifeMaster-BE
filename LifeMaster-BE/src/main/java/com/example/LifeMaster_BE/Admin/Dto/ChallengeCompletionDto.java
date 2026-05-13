package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChallengeCompletionDto {
    private Long totalParticipants;
    private Long completedParticipants;
    private double completionRate;
}