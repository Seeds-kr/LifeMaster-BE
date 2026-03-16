package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    // 회원 통계
    private long totalMembers;
    private long activeMembers;
    private long suspendedMembers;
    private long newMembersToday;
    private long newMembersThisWeek;

    // 콘텐츠 통계
    private long totalPosts;
    private long newPostsToday;
    private long totalComments;
    private long newCommentsToday;

    // 신고 통계
    private long totalReports;
    private long pendingReports;
    private long reviewingReports;
    private long resolvedReports;
}
