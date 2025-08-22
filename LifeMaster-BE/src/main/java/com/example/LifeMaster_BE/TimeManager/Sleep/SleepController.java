package com.example.LifeMaster_BE.TimeManager.Sleep;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sleep Management", description = "수면 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/sleep")
public class SleepController {

    private final SleepService sleepService;

    @Operation(summary = "유저의 수면 기록 조회", description = "사용자의 ID를 기반으로 수면 기록을 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<List<SleepDto.Response>> selectSleep(@PathVariable Long userId) {
        List<SleepDto.Response> responses = sleepService.selectSleep(userId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "수면 시작 기록", description = "사용자가 수면을 시작했을 때 기록을 생성합니다.")
    @PostMapping
    public ResponseEntity<String> makeSleep(@RequestBody SleepDto.Request request) {
        sleepService.makeSleep(request);
        return ResponseEntity.ok("Sleep started");
    }

    @Operation(summary = "수면 기록 업데이트", description = "사용자의 수면 기록을 업데이트합니다.")
    @PatchMapping
    public ResponseEntity<String> updateSleep(@RequestBody SleepDto.Request request) {
        sleepService.updateSleep(request);
        return ResponseEntity.ok("Sleep updated");
    }
}
