package com.example.LifeMaster_BE.Admin.Dto;

import com.example.LifeMaster_BE.Community.Post.PostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPostResponseDto {

    private Long postId;

    private String title;

    /**
     * 관리자 목록에서 표시할 내용 요약.
     */
    private String contentSummary;

    private String file;

    private PostType type;

    private Boolean calendarShared;

    private Long memberId;

    private String memberEmail;

    private String memberNickname;

    private int viewCount;

    private int commentCount;

    private long likeCount;

    private LocalDateTime createdAt;
}