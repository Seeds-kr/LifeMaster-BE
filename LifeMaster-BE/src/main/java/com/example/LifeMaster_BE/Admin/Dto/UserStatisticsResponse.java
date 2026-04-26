package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserStatisticsResponse {

    private Long memberId;
    private String nickname;
    private String email;

    private Long challengeCount;
    private Long groupCount;
    private Long postCount;
    private Long commentCount;
    private Long todoCount;

    private Long activityScore;
}
