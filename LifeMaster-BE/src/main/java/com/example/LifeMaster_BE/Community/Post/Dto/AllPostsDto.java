package com.example.LifeMaster_BE.Community.Post.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDateTime createdAt;
    private boolean liked;

}
