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

    @PostMapping
    public ResponseEntity<Void> createRepeatDetox(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RepeatDetoxDto.Request request
    ) {
        repeatDetoxService.createRepeatDetox(userDetails.getId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<RepeatDetoxDto.ListResponse> getRepeatDetox(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.getAllRepeatDetox(userDetails.getId())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepeatDetox(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        repeatDetoxService.deleteRepeatDetox(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lock-status")
    public ResponseEntity<List<RepeatDetoxDto.LockStatusResponse>> getLockStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.getLockStatus(userDetails.getId())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepeatDetoxDto.DetailResponse> getDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.getDetail(userDetails.getId(), id)
        );
    }

    @GetMapping("/{id}/generate-phrase")
    public ResponseEntity<RepeatDetoxDto.PhraseResponse> generatePhrase(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.generatePhrase(userDetails.getId(), id)
        );
    }

    @PostMapping("/{id}/verify-phrase")
    public ResponseEntity<Boolean> verifyPhrase(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam String phrase
    ) {
        return ResponseEntity.ok(
                repeatDetoxService.verifyPhrase(userDetails.getId(), id, phrase)
        );
    }

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