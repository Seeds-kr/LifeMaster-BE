package com.example.LifeMaster_BE.Community.Post.Dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllPostsDto {

    private String title;
    private String nickName;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private boolean liked;

}
