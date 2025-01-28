package com.example.LifeMaster_BE.Community.Post.Dto;

import com.example.LifeMaster_BE.Community.Post.PostType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostDto {

    private String title;
    private String content;
    private String file;
    private PostType type;
}
