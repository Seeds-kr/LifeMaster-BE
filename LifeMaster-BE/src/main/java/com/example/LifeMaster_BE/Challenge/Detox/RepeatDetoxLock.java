package com.example.LifeMaster_BE.Challenge.Detox;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepeatDetoxLock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long DetoxLockId;

    @Column
    private String DetoxName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detoxId", nullable = false)
    private RepeatDetox repeatDetox;
}
