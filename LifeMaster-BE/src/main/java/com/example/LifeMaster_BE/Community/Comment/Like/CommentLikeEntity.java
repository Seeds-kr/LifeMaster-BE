package com.example.LifeMaster_BE.Community.Comment.Like;

import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "comment_id"}))
public class CommentLikeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentEntity comment;

    public CommentLikeEntity(Long memberId, CommentEntity comment) {
        this.memberId = memberId;
        this.comment = comment;
    }
}
