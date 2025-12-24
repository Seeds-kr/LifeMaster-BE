package com.example.LifeMaster_BE.Community.Post.Dto;

import com.example.LifeMaster_BE.Community.Post.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateResponse {

    private Long postId;
    private String title;
    private String content;
    private String file;
    private PostType type;
    private boolean calendarShared;
}
