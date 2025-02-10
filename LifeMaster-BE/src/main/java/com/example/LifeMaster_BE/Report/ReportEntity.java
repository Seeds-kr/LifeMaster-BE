package com.example.LifeMaster_BE.Report;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class ReportEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ReportEntity(MemberEntity member, PostEntity post, String reason) {
        this.member = member;
        this.post = post;
        this.reason = reason;
    }
}
