package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/detox/repeat")
public class RepeatDetoxController {

    private final RepeatDetoxService repeatDetoxService;

    // 생성
    @PostMapping
    public ResponseEntity<Void> createRepeatDetox(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RepeatDetoxDto.Request request
    ) {
        repeatDetoxService.createRepeatDetox(userDetails.getId(), request);
        return ResponseEntity.ok().build();
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<RepeatDetoxDto.ListResponse> getRepeatDetox(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.getAllRepeatDetox(userDetails.getId())
        );
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepeatDetox(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        repeatDetoxService.deleteRepeatDetox(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    // 앱 실행 시 차단 여부
    @GetMapping("/lock-status")
    public ResponseEntity<List<RepeatDetoxDto.LockStatusResponse>> getLockStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.getLockStatus(userDetails.getId())
        );
    }

    // 상세 상태
    @GetMapping("/{id}")
    public ResponseEntity<RepeatDetoxDto.DetailResponse> getDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.getDetail(userDetails.getId(), id)
        );
    }

    // 비상탈출 문장 생성
    @GetMapping("/generate-phrase")
    public ResponseEntity<RepeatDetoxDto.PhraseResponse> generatePhrase() {
        return ResponseEntity.ok(
                repeatDetoxService.generatePhrase()
        );
    }

    // 비상탈출 검증
    @PostMapping("/verify-phrase")
    public ResponseEntity<Boolean> verifyPhrase(
            @RequestParam Long id,
            @RequestParam String phrase
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.verifyPhrase(id, phrase)
        );
    }

    // ================================
    // ✅ 핵심 추가 API
    // ================================

    /**
     * 프론트 UsageStats → 서버 동기화
     */
    @PatchMapping("/{id}/usage")
    public ResponseEntity<Void> syncUsage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody RepeatDetoxDto.UsageSyncRequest request
    ) {
        repeatDetoxService.syncUsage(
                userDetails.getId(),
                id,
                request.getTodayUsedMinutes()
        );

        return ResponseEntity.ok().build();
    }
}