package com.example.LifeMaster_BE.Challenge.Detox.PermanentDetox;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PermanentDetoxRequestDTO {

    @NotEmpty(message = "잠글 앱 목록은 비어 있을 수 없습니다.")
    @Schema(description = "영구 잠금 대상 앱 목록", example = "[\"com.kakao.talk\", \"com.instagram.android\"]")
    private List<String> lockedApps;
}