package com.example.LifeMaster_BE.Admin.Dto;

import com.example.LifeMaster_BE.Community.Post.PostType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostTypeCountDto {
    private PostType type;
    private Long count;
}
