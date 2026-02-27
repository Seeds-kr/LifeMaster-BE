package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Setter
@Getter
public class TimeDetoxEntity {
    // Getters and Setters

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cycle; // WEEKLY, BIWEEKLY
    private String day; // MONDAY, TUESDAY, etc.

    // startTime/endTime은 HH:mm 로 응답/요청 포맷 고정 (초는 안 보냄)
    @Schema(description = "Start time in HH:mm format", example = "10:30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "End time in HH:mm format", example = "18:30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    // active 필드는 “엔티티에 남겨두되”, API에는 숨김(토글 아이콘 없음 요구 충족)
    @JsonIgnore
    @Schema(hidden = true)
    private boolean isActive;

    @ElementCollection
    @CollectionTable(name = "detox_locked_apps", joinColumns = @JoinColumn(name = "detox_id"))
    @Column(name = "app_name")
    @Schema(description = "잠금 대상 앱 목록", example = "[\"YouTube\", \"Instagram\", \"Facebook\"]")
    private List<String> lockedApps; // 잠금 대상 앱 목록

    private LocalDate createdDate; // 스케줄 생성 날짜 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MemberEntity member;

    public boolean isActive() {
        return isActive;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now(); // 엔티티 생성 시 현재 날짜를 자동으로 설정
    }

    public void setMemberId(Long memberId) {
        this.member = new MemberEntity();
        this.member.setId(memberId);
    }

    public String getDate() {
        return this.createdDate != null
                ? this.createdDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : null;
    }
}
