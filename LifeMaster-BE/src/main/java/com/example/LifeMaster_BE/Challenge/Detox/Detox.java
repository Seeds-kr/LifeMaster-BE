package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Detox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long detoxId;

    @Column
    private Long detoxLock; // 잠금시간


    @Column
    private Long detoxUse; // 사용시간

    @Column
    private Long detoxBreack; // 사용시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

}
