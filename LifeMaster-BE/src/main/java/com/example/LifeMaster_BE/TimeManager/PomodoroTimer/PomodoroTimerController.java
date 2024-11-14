package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/time/pomodoro")
public class PomodoroTimerController {
    @Autowired
    private PomodoroTimerService service;

    // 모든 포모도로 타이머를 조회하는 메서드
    @GetMapping
    public List<PomodoroTimerEntity> getAllTimers() {
        return service.findAll();
    }

    // ID로 특정 포모도로 타이머를 조회하는 메서드
    @GetMapping("/id/{id}")
    public ResponseEntity<PomodoroTimerEntity> getTimerById(@PathVariable(name="id") Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 날짜로 포모도로 타이머 목록을 조회하는 메서드
    @GetMapping("/date/{date}")
    public List<PomodoroTimerEntity> getTimerByDate(@PathVariable(name="date") String date) {
        return service.findByDate(date);
    }

    // 새로운 포모도로 타이머를 생성하는 메서드
    @PostMapping("/create")
    public PomodoroTimerEntity createTimer(@RequestBody PomodoroTimerEntity timer) {
        return service.save(timer);
    }

    // ID로 특정 포모도로 타이머를 업데이트하는 메서드
    @PutMapping("/{id}")
    public ResponseEntity<PomodoroTimerEntity> updateTimer(@PathVariable(name="id") Long id, @RequestBody PomodoroTimerEntity timerDetails) {
        return service.findById(id).map(timer -> {
            timer.setTaskName(timerDetails.getTaskName());
            timer.setCurrentTimer(timerDetails.getCurrentTimer());
            timer.setFocusTime(timerDetails.getFocusTime());
            timer.setBreakTime(timerDetails.getBreakTime());
            timer.setCycles(timerDetails.getCycles());
            timer.setDate(timerDetails.getDate());
            return ResponseEntity.ok(service.save(timer));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ID로 특정 포모도로 타이머를 삭제하는 메서드
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteTimerById(@PathVariable(name="id") Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 날짜로 모든 포모도로 타이머를 삭제하는 메서드
    @DeleteMapping("/date/{date}")
    public ResponseEntity<Void> deleteTimerByDate(@PathVariable(name="date") String date) {
        service.deleteAllByDate(date);
        return ResponseEntity.noContent().build();
    }
}
