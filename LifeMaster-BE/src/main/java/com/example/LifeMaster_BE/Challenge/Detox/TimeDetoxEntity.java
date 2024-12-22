package com.example.LifeMaster_BE.Challenge.Detox;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
public class TimeDetoxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cycle; // WEEKLY, BIWEEKLY
    private String day; // MONDAY, TUESDAY, etc.

    @Schema(description = "Start time in HH:mm:ss format", example = "10:30:00")
    private LocalTime startTime;

    @Schema(description = "End time in HH:mm:ss format", example = "18:30:00")
    private LocalTime endTime;

    private boolean isActive;

    @ElementCollection
    @CollectionTable(name = "detox_locked_apps", joinColumns = @JoinColumn(name = "detox_id"))
    @Column(name = "app_name")
    @Schema(description = "잠금 대상 앱 목록", example = "[\"YouTube\", \"Instagram\", \"Facebook\"]")
    private List<String> lockedApps; // 잠금 대상 앱 목록

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<String> getLockedApps() {
        return lockedApps;
    }

    public void setLockedApps(List<String> lockedApps) {
        this.lockedApps = lockedApps;
    }
}
