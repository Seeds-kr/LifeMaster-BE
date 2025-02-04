package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Like.CommentLikeEntity;
import com.example.LifeMaster_BE.Community.Post.PostEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Auditing 설정
public class CommentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50)
    private String comment;

    @CreatedDate
    private LocalDateTime commentDate;

    @OneToMany(mappedBy = "comment")
    private List<CommentLikeEntity> likes = new ArrayList<>();

    // 게시글 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity post;

    // 작성자 연결

    public void updateComment(String newComment) {
        if (newComment == null || newComment.isBlank()){
            throw new IllegalArgumentException("Comment cannot be blank");
        }

        this.comment = newComment;
    }
    public CommentEntity(String comment, PostEntity post) {
        this.comment = comment;
        this.post = post;
    }
}
