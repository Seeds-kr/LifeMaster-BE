package com.example.LifeMaster_BE.Challenge;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChallengeUser {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challId")
    private Challenge challenge;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private MemberEntity user;
}
