package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "White Noise Playlist", description = "백색소음 재생목록 관리 API")
@RestController
@RequestMapping("/time/sleep/playlist")
public class WhiteNoiseController {

    private final WhiteNoiseService service;

    private final Login login;

    public WhiteNoiseController(WhiteNoiseService service, Login login) {
        this.service = service;
        this.login = login;
    }

    @Operation(summary = "전체 백색소음 조회", description = "모든 백색소음 항목을 조회합니다.")
    @GetMapping
    public List<WhiteNoiseEntity> getAllWhiteNoises() {
        return service.findAll();
    }

    @Operation(summary = "ID로 백색소음 조회", description = "ID를 기준으로 특정 백색소음 항목을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<WhiteNoiseEntity> getWhiteNoiseById(@PathVariable(name = "id") Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "백색소음 생성", description = "새로운 백색소음 항목을 생성합니다.")
    @PostMapping
    public ResponseEntity<?> createWhiteNoise(@RequestBody WhiteNoiseEntity whiteNoise, @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        Long memberId = user.getId();
        return new ResponseEntity<>(service.save(whiteNoise), HttpStatus.CREATED);
    }

    @Operation(summary = "백색소음 수정", description = "기존의 백색소음 항목을 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<WhiteNoiseEntity> updateWhiteNoise(@PathVariable(name = "id") Long id, @RequestBody WhiteNoiseEntity whiteNoise) {
        return service.findById(id)
                .map(existingNoise -> {
                    whiteNoise.setId(existingNoise.getId());
                    return ResponseEntity.ok(service.save(whiteNoise));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "백색소음 삭제", description = "ID를 기준으로 백색소음 항목을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWhiteNoise(@PathVariable(name = "id") Long id) {
        if (service.findById(id).isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

