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
public class TimeLock {// 시간잠금 시 허용할 어플 목록
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long TimeLockId;

    @Column
    private String TimeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeDetoxId", nullable = false)
    private TimeDetoxEntity timedetox;
}
