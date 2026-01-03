package com.example.LifeMaster_BE.Community.Post.Dto;

import com.example.LifeMaster_BE.Community.Post.PostType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostCreateRequest {

    private String title;
    private String content;
    private String file;
    private PostType type;
    private Boolean calendarShared;
}
