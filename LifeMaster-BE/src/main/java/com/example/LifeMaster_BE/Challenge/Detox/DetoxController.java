package com.example.LifeMaster_BE.Challenge.Detox;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/detox")
public class DetoxController {

    private final DetoxService detoxService;

    // 1. 잠금 설정 추가
    @PostMapping
    public ResponseEntity<Detox> addRepeatedLock(@RequestBody Long userId, @RequestBody DetoxDto.Request request) {
        Detox detox = detoxService.addRepeatedLock(userId, request);
        return ResponseEntity.ok(detox);
    }

    // 2. 특정 사용자 잠금 설정 조회
    @GetMapping
    public ResponseEntity<List<Detox>> getRepeatedLocksByUser(@RequestBody Long userId) {
        List<Detox> locks = detoxService.getRepeatedLocksByUser(userId);
        return ResponseEntity.ok(locks);
    }

    // 3. 잠금 설정 업데이트
    @PutMapping("/{lockId}")
    public ResponseEntity<Detox> updateRepeatedLock(@PathVariable Long lockId, @RequestBody DetoxDto.Request request) {
        Detox updatedLock = detoxService.updateRepeatedLock(lockId, request);
        return ResponseEntity.ok(updatedLock);
    }

    // 4. 잠금 설정 삭제
    @DeleteMapping("/{lockId}")
    public ResponseEntity<Void> deleteRepeatedLock(@PathVariable Long lockId) {
        detoxService.deleteRepeatedLock(lockId);
        return ResponseEntity.noContent().build();
    }
}

