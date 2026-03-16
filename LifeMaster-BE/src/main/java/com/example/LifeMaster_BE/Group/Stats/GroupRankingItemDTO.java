package com.example.LifeMaster_BE.Group.Stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 랭킹 항목")
public class GroupRankingItemDTO {

    @Schema(description = "순위", example = "1")
    private Integer rank;

    @Schema(description = "회원 ID", example = "10")
    private Long memberId;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "프로필 이미지", example = "url")
    private String profileImage;

    @Schema(description = "달성 횟수", example = "3")
    private Integer achieveCount;
}