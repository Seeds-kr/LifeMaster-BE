package com.example.LifeMaster_BE.Community.Post.Like;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "post_id"}))
public class PostLikeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Setter
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    public PostLikeEntity(Long memberId) {
        this.memberId = memberId;
    }
}
