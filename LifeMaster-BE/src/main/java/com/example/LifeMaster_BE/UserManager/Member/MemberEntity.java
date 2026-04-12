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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "member_entity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_email", columnNames = "email"),
                @UniqueConstraint(name = "idx_member_nickname", columnNames = "nickname")
        }

)
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL 호환 전략
    private Long id;

    @Column(name = "email", nullable = false) // 이메일 중복 방지
    private String email;

    @Column(nullable = false)
    private String password; // 연동 로그인 유저는 고유 식별 ID

    @Column(nullable = false)
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

    // 관리자 기능용 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "member_status")
    private MemberStatus memberStatus = MemberStatus.ACTIVE;

    @Column(name = "warning_count")
    private int warningCount = 0;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    // Many-to-Many 관계 추가
    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "member_group", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "member_id"), // 현재 엔티티를 참조하는 외래 키
            inverseJoinColumns = @JoinColumn(name = "group_id") // 상대 엔티티를 참조하는 외래 키
    )
    private Set<GroupEntity> groups = new HashSet<>(); // 그룹 목록

    // ⭐ 요금제 관련 추가 ⭐
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE; // 기본 요금제: 무료

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate; // 마지막 결제일

    @Column(name = "expiration_date")
    private LocalDate expirationDate; // 결제 만료일

    public boolean hasActivePremiumAccess() {
        return this.subscriptionPlan == SubscriptionPlan.PREMIUM
                && this.paymentStatus == PaymentStatus.PAID
                && this.expirationDate != null
                && !this.expirationDate.isBefore(LocalDate.now());
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID; // 기본 상태: 미결제

    // 결제 내역
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PaymentEntity> payments = new ArrayList<>();

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

    public MemberEntity(String email, String encodedPassword, String nickname) {
        this.email = email;
        this.password = encodedPassword;
        this.nickname = nickname;
    }

    public String getName() {
        return this.nickname;
    }

    public String getPicture() {
        return this.imageUrl;
    }

    public MemberEntity update(String picture, String nickname) {
        if (picture != null) this.imageUrl = picture;

        // NOT NULL 방어 (null/blank면 기존 값 유지)
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }

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

    public LocalDate getSubscriptionExpirationDate() {
        return expirationDate;
    }
}