package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DetoxVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저별로 하나만 갖게 하고 싶으면 OneToOne + unique
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true)
    private MemberEntity member;

    @Column(nullable = false)
    private String phrase;  // 검증용 문구

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 선택: 만료시간 넣고 싶으면
    private LocalDateTime expiresAt;

    // 선택: 한 번 검증되면 재사용 막고 싶을 때
    private boolean used;

    public static DetoxVerificationEntity create(MemberEntity member, String phrase, LocalDateTime expiresAt) {
        DetoxVerificationEntity e = new DetoxVerificationEntity();
        e.setMember(member);
        e.setPhrase(phrase);
        e.setCreatedAt(LocalDateTime.now());
        e.setExpiresAt(expiresAt);
        e.setUsed(false);
        return e;
    }
}

