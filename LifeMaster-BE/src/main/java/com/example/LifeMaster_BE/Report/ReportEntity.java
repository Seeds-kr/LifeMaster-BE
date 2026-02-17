package com.example.LifeMaster_BE.Report;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_report_member_post", columnList = "member_id, post_id")
        }
)
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

    // 관리자 기능용 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReportStatus status = ReportStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ReportActionType actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private MemberEntity resolvedBy;

    @Column(name = "admin_note")
    private String adminNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public ReportEntity(MemberEntity member, PostEntity post, String reason) {
        this.member = member;
        this.post = post;
        this.reason = reason;
    }

    public void resolve(MemberEntity admin, ReportStatus status, ReportActionType actionType, String adminNote) {
        this.resolvedBy = admin;
        this.status = status;
        this.actionType = actionType;
        this.adminNote = adminNote;
        this.resolvedAt = LocalDateTime.now();
    }

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }
}
