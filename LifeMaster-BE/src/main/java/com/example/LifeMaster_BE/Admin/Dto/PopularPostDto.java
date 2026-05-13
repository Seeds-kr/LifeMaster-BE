package com.example.LifeMaster_BE.Admin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PopularPostDto {
    private Long postId;
    private String title;
    private int viewCount;
    private int commentCount;
    private int likeCount;
    private int score;
}