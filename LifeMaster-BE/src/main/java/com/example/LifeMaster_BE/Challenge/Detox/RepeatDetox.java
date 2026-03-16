package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepeatDetox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lockedApp; // 반복 잠금할 앱 패키지명

    @Column(nullable = false)
    private Integer sessionUsageLimit; // 1회 사용 시간 (분)

    @Column(nullable = false)
    private Integer lockDuration; // 1회 잠금 시간 (분)

    @Column
    private Integer dailyMaxUsageLimit; // 하루 최대 사용 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
}