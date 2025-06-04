package com.example.LifeMaster_BE.Community.Comment.Like;

import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Setter
@NoArgsConstructor
@Table(
        name = "comment_likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_comment_like_member_post", columnNames = {"member_id", "comment_id"})
        }
)
public class CommentLikeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Setter
    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentEntity comment;

    public CommentLikeEntity(Long memberId) {
        this.memberId = memberId;
    }
}
