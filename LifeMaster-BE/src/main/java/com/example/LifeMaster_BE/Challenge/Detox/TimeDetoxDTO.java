package com.example.LifeMaster_BE.Challenge.Detox;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TimeDetoxDTO {
    private Long id;
    private String cycle;
    private String day;
    private String startTime;
    private String endTime;
    private boolean active;
    private List<String> lockedApps;
}
