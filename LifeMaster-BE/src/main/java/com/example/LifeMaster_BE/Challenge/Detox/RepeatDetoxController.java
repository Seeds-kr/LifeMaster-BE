package com.example.LifeMaster_BE.Challenge.Detox;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/detox/repeat")
public class RepeatDetoxController {

    private final RepeatDetoxService repeatDetoxService;

    // 생성
    @Operation(summary = "반복 디톡스 생성", description = "요일과 시간 정보를 기반으로 반복되는 앱 잠금(디톡스) 설정을 생성합니다.")
    @PostMapping
    public ResponseEntity<Void> createRepeatDetox(
            @RequestBody RepeatDetoxDto.Request request
    ) {

        repeatDetoxService.createRepeatDetox(request);
        return ResponseEntity.ok().build();
    }

    // 조회
    @Operation(summary = "반복 디톡스 목록 조회", description = "사용자가 설정한 반복 디톡스 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<RepeatDetoxDto.ListResponse> getRepeatDetox() {

        return ResponseEntity.ok(
                repeatDetoxService.getAllRepeatDetox()
        );
    }

    // 삭제
    @Operation(summary = "반복 디톡스 삭제", description = "특정 반복 디톡스 설정을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepeatDetox(
            @PathVariable Long id
    ) {

        repeatDetoxService.deleteRepeatDetox(id);
        return ResponseEntity.noContent().build();
    }
}