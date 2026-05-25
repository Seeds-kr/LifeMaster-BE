package com.example.LifeMaster_BE.Challenge;

import com.example.LifeMaster_BE.ETC.BaseEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_completion")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeCompletion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challId")
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private MemberEntity user;

    // 완료 시간 (UI 표시용 핵심)
    @Column
    private LocalDateTime completedAt;

    // "오늘 완료 여부" 체크용 날짜 키
    @Column
    private String dateKey; // yyyyMMdd
}
