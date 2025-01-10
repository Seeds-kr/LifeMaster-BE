package com.example.LifeMaster_BE.Community.Comment.Dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllCommentsDto {

    private String authorName;
    private String comment;
    private LocalDateTime commentDate;

}
