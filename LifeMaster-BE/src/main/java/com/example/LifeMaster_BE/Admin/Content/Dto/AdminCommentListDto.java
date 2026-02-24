package com.example.LifeMaster_BE.Admin.Content.Dto;

import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentListDto {

    private Long id;
    private String comment;
    private Long postId;
    private String postTitle;
    private String authorNickname;
    private Long authorId;
    private int likeCount;
    private LocalDateTime createdAt;

    public static AdminCommentListDto from(CommentEntity commentEntity) {
        return AdminCommentListDto.builder()
                .id(commentEntity.getId())
                .comment(commentEntity.getComment())
                .postId(commentEntity.getPost() != null ? commentEntity.getPost().getId() : null)
                .postTitle(commentEntity.getPost() != null ? commentEntity.getPost().getTitle() : null)
                .authorNickname(commentEntity.getMember() != null ? commentEntity.getMember().getNickname() : null)
                .authorId(commentEntity.getMember() != null ? commentEntity.getMember().getId() : null)
                .likeCount(commentEntity.getLikes() != null ? commentEntity.getLikes().size() : 0)
                .createdAt(commentEntity.getCreatedAt())
                .build();
    }
}
