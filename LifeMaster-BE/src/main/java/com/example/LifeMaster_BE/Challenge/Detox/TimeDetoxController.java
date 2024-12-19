package com.example.LifeMaster_BE.Challenge.Detox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/detox")
public class TimeDetoxController {

    @Autowired
    private TimeDetoxService service;

    @PostMapping
    public ResponseEntity<TimeDetoxEntity> createSchedule(@RequestBody TimeDetoxEntity schedule) {
        return ResponseEntity.ok(service.createSchedule(schedule));
    }

    @GetMapping
    public ResponseEntity<List<TimeDetoxEntity>> getAllSchedules() {
        return ResponseEntity.ok(service.getAllSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeDetoxEntity> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getScheduleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeDetoxEntity> updateSchedule(@PathVariable Long id, @RequestBody TimeDetoxEntity updatedSchedule) {
        return ResponseEntity.ok(service.updateSchedule(id, updatedSchedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        service.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lock-status")
    public ResponseEntity<Boolean> isAppLocked(@RequestParam String day, @RequestParam LocalTime currentTime) {
        return ResponseEntity.ok(service.isAppLocked(day, currentTime));
    }
}

