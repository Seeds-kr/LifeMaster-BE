package com.example.LifeMaster_BE.Challenge.Detox;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/detox/repeat")
public class RepeatDetoxController {

    private final RepeatDetoxService repeatDetoxService;

    // 생성
    @PostMapping
    public ResponseEntity<Void> createRepeatDetox(
            @RequestBody RepeatDetoxDto.Request request
    ) {

        repeatDetoxService.createRepeatDetox(request);
        return ResponseEntity.ok().build();
    }

    // 조회
    @GetMapping
    public ResponseEntity<RepeatDetoxDto.ListResponse> getRepeatDetox() {

        return ResponseEntity.ok(
                repeatDetoxService.getAllRepeatDetox()
        );
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepeatDetox(
            @PathVariable Long id
    ) {

        repeatDetoxService.deleteRepeatDetox(id);
        return ResponseEntity.noContent().build();
    }
}