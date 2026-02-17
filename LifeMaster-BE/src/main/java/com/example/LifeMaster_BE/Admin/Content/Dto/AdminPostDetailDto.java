package com.example.LifeMaster_BE.Admin.Content.Dto;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPostDetailDto {

    private Long id;
    private String title;
    private String content;
    private String file;
    private PostType type;
    private Boolean calendarShared;
    private String authorNickname;
    private Long authorId;
    private String authorEmail;
    private int viewCount;
    private int commentCount;
    private int likeCount;
    private int reportCount;
    private LocalDateTime createdAt;

    public static AdminPostDetailDto from(PostEntity post) {
        return AdminPostDetailDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .file(post.getFile())
                .type(post.getType())
                .calendarShared(post.getCalendarShared())
                .authorNickname(post.getMember() != null ? post.getMember().getNickname() : null)
                .authorId(post.getMember() != null ? post.getMember().getId() : null)
                .authorEmail(post.getMember() != null ? post.getMember().getEmail() : null)
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
                .reportCount(post.getReports() != null ? post.getReports().size() : 0)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
