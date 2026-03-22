package com.example.LifeMaster_BE.Challenge.Detox.PermanentDetox;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/detox/permanent")
@RequiredArgsConstructor
public class PermanentDetoxController {

    private final PermanentDetoxService permanentDetoxService;

    @Operation(
            summary = "영구 잠금 디톡스 생성",
            description = "로그인한 사용자의 영구 잠금 디톡스를 생성합니다. 이미 존재하는 경우 중복 생성할 수 없습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "영구 잠금할 앱 목록",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PermanentDetoxRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "lockedApps": [
                                        "com.kakao.talk",
                                        "com.naver.line.android",
                                        "com.instagram.android",
                                        "com.facebook.katana",
                                        "com.coupang.mobile"
                                      ]
                                    }
                                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "영구 잠금 디톡스 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PermanentDetoxResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "lockedApps": [
                                                "com.kakao.talk",
                                                "com.naver.line.android",
                                                "com.instagram.android",
                                                "com.facebook.katana",
                                                "com.coupang.mobile"
                                              ]
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 영구 잠금 디톡스가 존재함"),
                    @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음")
            }
    )
    @PostMapping
    public ResponseEntity<PermanentDetoxResponseDTO> createPermanentDetox(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody PermanentDetoxRequestDTO requestDTO
    ) {
        Long memberId = user.getId();
        return ResponseEntity.ok(permanentDetoxService.createPermanentDetox(memberId, requestDTO));
    }

    @Operation(
            summary = "영구 잠금 디톡스 조회",
            description = "로그인한 사용자의 영구 잠금 앱 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "영구 잠금 앱 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PermanentDetoxResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "lockedApps": [
                                                "com.kakao.talk",
                                                "com.naver.line.android",
                                                "com.instagram.android",
                                                "com.facebook.katana",
                                                "com.coupang.mobile"
                                              ]
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "영구 잠금 디톡스가 존재하지 않음")
            }
    )
    @GetMapping
    public ResponseEntity<PermanentDetoxResponseDTO> getPermanentDetox(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        return ResponseEntity.ok(permanentDetoxService.getPermanentDetox(memberId));
    }

    @Operation(
            summary = "영구 잠금 디톡스 수정",
            description = "로그인한 사용자의 영구 잠금 앱 목록을 수정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 영구 잠금 앱 목록",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PermanentDetoxRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "lockedApps": [
                                        "com.kakao.talk",
                                        "com.instagram.android"
                                      ]
                                    }
                                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "영구 잠금 디톡스 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PermanentDetoxResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "lockedApps": [
                                                "com.kakao.talk",
                                                "com.instagram.android"
                                              ]
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터"),
                    @ApiResponse(responseCode = "404", description = "영구 잠금 디톡스가 존재하지 않음")
            }
    )
    @PutMapping
    public ResponseEntity<PermanentDetoxResponseDTO> updatePermanentDetox(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody PermanentDetoxRequestDTO requestDTO
    ) {
        Long memberId = user.getId();
        return ResponseEntity.ok(permanentDetoxService.updatePermanentDetox(memberId, requestDTO));
    }

    @Operation(
            summary = "영구 잠금 디톡스 삭제",
            description = "로그인한 사용자의 영구 잠금 디톡스를 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "영구 잠금 디톡스 삭제 성공",
                            content = @Content(
                                    mediaType = "text/plain",
                                    examples = @ExampleObject(value = "영구 잠금 디톡스가 삭제되었습니다.")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "영구 잠금 디톡스가 존재하지 않음")
            }
    )
    @DeleteMapping
    public ResponseEntity<String> deletePermanentDetox(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        permanentDetoxService.deletePermanentDetox(memberId);
        return ResponseEntity.ok("영구 잠금 디톡스가 삭제되었습니다.");
    }
}
