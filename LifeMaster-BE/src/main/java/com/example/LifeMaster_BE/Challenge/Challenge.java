package com.example.LifeMaster_BE.Challenge;

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
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challId;

    @Column
    private String challName;

    @Column
    private String challDesc;

    @Column
    private String challImg;

    @Column
    @Builder.Default
    private Integer challCnt = 1;
}
