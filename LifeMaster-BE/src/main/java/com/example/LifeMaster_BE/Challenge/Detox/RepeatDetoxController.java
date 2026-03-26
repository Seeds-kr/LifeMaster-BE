package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 조회
    @GetMapping
    public ResponseEntity<RepeatDetoxDto.ListResponse> getRepeatDetox(@AuthenticationPrincipal CustomUserDetails userDetails) {

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
}