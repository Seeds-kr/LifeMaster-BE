package com.example.LifeMaster_BE.TimeManager.Sleep;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/time/sleep")
public class SleepController {

    private final SleepService sleepService;

    @PostMapping
    ResponseEntity<List<SleepDto.Response>> selectSleep(SleepDto.Request request){
        List<SleepDto.Response> responses = sleepService.selectSleep(request.getUserId());
        return ResponseEntity.ok(responses);
    }

    @PutMapping
    ResponseEntity<String> makeSleep(SleepDto.Request request){
        sleepService.makeSleep(request);
        return ResponseEntity.ok("Sleep started");
    }

    @PatchMapping
    ResponseEntity<String> updateSleep(SleepDto.Request request){
        sleepService.updateSleep(request);
        return ResponseEntity.ok("Sleep updated");
    }
}
