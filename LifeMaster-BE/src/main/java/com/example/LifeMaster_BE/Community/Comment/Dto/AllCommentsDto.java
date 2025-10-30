package com.example.LifeMaster_BE.Community.Comment.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllCommentsDto {

    private Long commentId;
    private Long memberId;
    private String comment;
    private String nickname;
    private int likeCount;
    private boolean liked;

    @JsonProperty("isMine")
    private boolean mine;
    private LocalDateTime commentDate;
}
