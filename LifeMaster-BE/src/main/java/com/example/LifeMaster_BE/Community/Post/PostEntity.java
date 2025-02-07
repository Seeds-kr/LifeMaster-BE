package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import com.example.LifeMaster_BE.Community.Post.Like.PostLikeEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class PostEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private String file;
    // 게시글 유형
    @Enumerated(EnumType.STRING)
    private PostType type;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @OneToMany(mappedBy = "post")
    private List<PostLikeEntity> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<CommentEntity> comments = new ArrayList<>();

    public PostEntity(String title, String content, String file, PostType type, MemberEntity member) {
        this.title = title;
        this.content = content;
        this.file = file;
        this.type = type;
        this.member = member;
    }

    public void updatePost(String title, String content, String fileUrl) {
        this.title = title;
        this.content = content;
        this.file = fileUrl;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // 현재 시간을 자동으로 설정
    }
}
