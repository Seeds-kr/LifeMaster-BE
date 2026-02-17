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
public class AdminPostListDto {

    private Long id;
    private String title;
    private PostType type;
    private String authorNickname;
    private Long authorId;
    private int viewCount;
    private int commentCount;
    private int likeCount;
    private int reportCount;
    private LocalDateTime createdAt;

    public static AdminPostListDto from(PostEntity post) {
        return AdminPostListDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .type(post.getType())
                .authorNickname(post.getMember() != null ? post.getMember().getNickname() : null)
                .authorId(post.getMember() != null ? post.getMember().getId() : null)
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
                .reportCount(post.getReports() != null ? post.getReports().size() : 0)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
