package com.example.LifeMaster_BE.Group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GroupPasswordUpdateRequest {

    @Schema(description = "OWNER 계정 비밀번호(재확인용)", example = "ownerPassword1234")
    private String ownerPassword;

    @Schema(description = "새 그룹 비밀번호(비우면 제거)", example = "newGroupPass!234", nullable = true)
    private String newGroupPassword;
}
