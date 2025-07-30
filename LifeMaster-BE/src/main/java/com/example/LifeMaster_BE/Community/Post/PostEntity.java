package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import com.example.LifeMaster_BE.Community.Post.Like.PostLikeEntity;
import com.example.LifeMaster_BE.Report.ReportEntity;
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

@Getter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        indexes = {
                @Index(name = "idx_type", columnList = "type"),
                @Index(name = "idx_viewCount", columnList = "view_count")
        }
)
public class PostEntity {

    @Id
    @Setter // 테스트 용
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
    @CreatedDate
    private LocalDateTime createdAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLikeEntity> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportEntity> reports = new ArrayList<>();

    private int commentCount = 0;

    public PostEntity(String title, String content, String file, PostType type) {
        this.title = title;
        this.content = content;
        this.file = file;
        this.type = type;
    }

    public void updatePost(String title, String content, String fileUrl) {
        this.title = title;
        this.content = content;
        this.file = fileUrl;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void addComment(CommentEntity comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public void addLike(PostLikeEntity like) {
        this.likes.add(like);
        like.setPost(this);
    }

    public void addReport(ReportEntity report) {
        this.reports.add(report);
        report.setPost(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // 현재 시간을 자동으로 설정
    }
}
