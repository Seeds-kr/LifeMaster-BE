package com.example.LifeMaster_BE.Challenge.Detox.PermanentDetox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Table(
        name = "permanent_detox",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "member_id")
        }
)
public class PermanentDetoxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "permanent_detox_locked_apps", joinColumns = @JoinColumn(name = "permanent_detox_id"))
    @Column(name = "app_name")
    @Schema(description = "영구 잠금 대상 앱 목록", example = "[\"com.kakao.talk\", \"com.instagram.android\"]")
    private List<String> lockedApps;

    private LocalDate createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private MemberEntity member;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now();
    }

    public void setMemberId(Long memberId) {
        this.member = new MemberEntity();
        this.member.setId(memberId);
    }
}
