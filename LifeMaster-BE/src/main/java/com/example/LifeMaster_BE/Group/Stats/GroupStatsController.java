package com.example.LifeMaster_BE.Group.Stats;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups/{groupId}/stats")
public class GroupStatsController {

    private final GroupStatsService groupStatsService;

    @GetMapping("/ranking")
    @Operation(summary = "그룹 내 랭킹 조회", description = "주간/전체 기준 그룹 멤버 랭킹을 조회합니다.")
    public GroupRankingResponseDTO getGroupRanking(
            @PathVariable Long groupId,
            @Parameter(description = "조회 범위", example = "WEEKLY")
            @RequestParam RankingScope scope,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return groupStatsService.getGroupRanking(groupId, user.getId(), scope);
    }
}
