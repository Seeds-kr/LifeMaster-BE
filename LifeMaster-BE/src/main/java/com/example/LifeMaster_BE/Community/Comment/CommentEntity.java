package com.example.LifeMaster_BE.Community.Comment;

import com.example.LifeMaster_BE.Community.Comment.Like.CommentLikeEntity;
import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    // 작성자 연결
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    // 게시글 연결
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity post;

    // 좋아요
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentLikeEntity> likes = new ArrayList<>();

    public void updateComment(String newComment) {
        if (newComment == null || newComment.isBlank()){
            throw new IllegalArgumentException("Comment cannot be blank");
        }

        this.comment = newComment;
    }

    public void addLike(CommentLikeEntity like) {
        this.likes.add(like);
        like.setComment(this);
    }
    public CommentEntity(String comment, PostEntity post) {
        this.comment = comment;
        this.post = post;
    }
}
