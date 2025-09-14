package com.example.LifeMaster_BE.Community.Post.Dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllPostsDto {

    private Long id;
    private String title;
    private String nickName;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private boolean liked;

}
