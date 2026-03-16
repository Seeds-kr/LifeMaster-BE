package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @Operation(summary = "전체 백색소음 조회")
    @GetMapping
    public ResponseEntity<List<WhiteNoiseDTO>> getAllWhiteNoises(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MemberEntity member = login.findMember(userDetails.getEmail()); // 🔑 이메일로 MemberEntity 조회
        List<WhiteNoiseEntity> entities = service.getAllForUser(member); // 🔄 해당 유저의 데이터만 조회

        List<WhiteNoiseDTO> dtoList = entities.stream()
                .map(WhiteNoiseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "ID로 백색소음 조회")
    @GetMapping("/{id}")
    public ResponseEntity<?> getWhiteNoiseById(@PathVariable(name = "id") Long id, @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "백색소음 생성")
    @PostMapping
    public ResponseEntity<?> createWhiteNoise(@RequestBody WhiteNoiseDTO dto, @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        Long memberId = user.getId();
        return new ResponseEntity<>(service.create(dto, memberId), HttpStatus.CREATED);
    }

    @Operation(summary = "백색소음 수정")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWhiteNoise(@PathVariable(name = "id") Long id, @RequestBody WhiteNoiseDTO dto, @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        return service.save(id, dto);
    }

    @Operation(summary = "백색소음 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWhiteNoise(@PathVariable(name = "id") Long id, @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        if (service.findById(id).isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
