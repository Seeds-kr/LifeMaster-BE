package com.example.LifeMaster_BE.UserManager.Member;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Comment.CommentEntity;
import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.Report.ReportEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankEntity;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentEntity;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentStatus;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionPlan;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "member_entity",
        indexes = {
                @Index(name = "idx_member_email", columnList = "email"),
                @Index(name = "idx_member_nickname", columnList = "nickname")
        }

)  // 테이블 이름을 단순화하여 충돌 방지
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL 호환 전략
    private Long id;

    @Column(name = "email", nullable = false, unique = true) // 이메일 중복 방지
    private String email;

    @Column(nullable = false)
    private String password; // 연동 로그인 유저는 고유 식별 ID

    private String nickname;

    @Column(name = "image_url") // 컬럼 이름 명시
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type") // 컬럼 이름 명시
    private LoginType loginType;

    @Column(name = "login_status") // 컬럼 이름 명시
    private boolean loginStatus;

    // Many-to-Many 관계 추가
    @Enumerated(EnumType.STRING)
    @Column
    private LoginRole loginRole = LoginRole.USER;

    // Many-to-Many 관계 추가
    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "member_group", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "member_id"), // 현재 엔티티를 참조하는 외래 키
            inverseJoinColumns = @JoinColumn(name = "group_id") // 상대 엔티티를 참조하는 외래 키
    )
    private Set<GroupEntity> groups = new HashSet<>(); // 그룹 목록

    // 게시글
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostEntity> posts = new ArrayList<>();

    // ⭐ 요금제 관련 추가 ⭐
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE; // 기본 요금제: 무료

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate; // 마지막 결제일

    @Column(name = "expiration_date")
    private LocalDate expirationDate; // 결제 만료일

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID; // 기본 상태: 미결제

    // 결제 내역
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PaymentEntity> payments = new ArrayList<>();

    // 댓글
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();

    // 5감사
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThankEntity> thanks = new ArrayList<>();

    // 일기
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryEntity> diaries = new ArrayList<>();

    // 신고
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportEntity> reports = new ArrayList<>();

    // 알람
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlarmEntity> alarms = new ArrayList<>();

    public MemberEntity(String email, String password) {
        this.email = email;
        this.password = password;
        this.loginStatus = true;
    }

    public String getName() {
        return this.nickname;
    }

    public String getPicture() {
        return this.imageUrl;
    }

    public MemberEntity update(String name, String picture) {
        this.nickname = name;
        this.imageUrl = picture;
        return this;
    }

    public void updateByMyPage(String nickname, String email){
        this.nickname = nickname;
        this.email = email;
    }

    public MemberEntity login(boolean bool){
        this.loginStatus = bool;
        return this;
    }

    // 게시글
    public void addPost(PostEntity post) {
        this.posts.add(post);
        post.setMember(this);
    }

    // 댓글
    public void addComment(CommentEntity comment) {
        this.comments.add(comment);
        comment.setMember(this);
    }

    // 5감사
    public void addThank(ThankEntity thank) {
        this.thanks.add(thank);
        thank.setMember(this);
    }

    // 일기
    public void addDiary(DiaryEntity diary) {
        this.diaries.add(diary);
        diary.setMember(this);
    }

    // 신고
    public void addReport(ReportEntity report) {
        this.reports.add(report);
        report.setMember(this);
    }

    // 알람
    public void addAlarm(AlarmEntity alarm){
        this.alarms.add(alarm);
        alarm.setMember(this);
    }

    // 요금제 변경 메서드
    public void updateSubscription(SubscriptionPlan plan, LocalDate lastPaymentDate, LocalDate expirationDate) {
        this.subscriptionPlan = plan;
        this.lastPaymentDate = lastPaymentDate;
        this.expirationDate = expirationDate;
        this.paymentStatus = PaymentStatus.PAID;
    }
}

