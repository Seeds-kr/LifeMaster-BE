package com.example.LifeMaster_BE.Group.Stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 랭킹 응답")
public class GroupRankingResponseDTO {

    @Schema(description = "조회 범위", example = "weekly")
    private String scope;

    @Schema(description = "내 순위", example = "36")
    private Integer myRank;

    @Schema(description = "내 회원 ID", example = "10")
    private Long memberId;

    @Schema(description = "내 닉네임", example = "홍길동")
    private String myName;

    @Schema(description = "내 프로필 이미지", example = "url")
    private String profileImage;

    @Schema(description = "내 달성 횟수", example = "3")
    private Integer achieveCount;

    @Schema(description = "랭킹 목록")
    private List<GroupRankingItemDTO> items;
}