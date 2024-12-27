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
public class DetoxLock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long DetoxLockId;

    @Column
    private String DetoxName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detoxId", nullable = false)
    private Detox detox;
}
