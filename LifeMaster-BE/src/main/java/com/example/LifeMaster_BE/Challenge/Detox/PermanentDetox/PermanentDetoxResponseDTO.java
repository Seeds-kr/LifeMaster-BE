package com.example.LifeMaster_BE.Challenge.Detox.PermanentDetox;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PermanentDetoxResponseDTO {
    private List<String> lockedApps;
}