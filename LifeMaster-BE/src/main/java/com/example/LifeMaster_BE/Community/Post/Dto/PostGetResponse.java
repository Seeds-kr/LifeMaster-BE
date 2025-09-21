package com.example.LifeMaster_BE.Community.Post.Dto;

import com.example.LifeMaster_BE.Community.Post.PostType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostGetResponse {

    private String title;
    private String content;
    private String file;
    private PostType type;
    private Long memberId;
    private String nickname;
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime createdAt;
}
