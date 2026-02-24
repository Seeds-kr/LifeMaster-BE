package com.example.LifeMaster_BE.Group;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupResponseDto {
    private Long id;
    private String icon;
    private String name;
    private String description;
    private Long creatorId;
    private long memberCount;
}

