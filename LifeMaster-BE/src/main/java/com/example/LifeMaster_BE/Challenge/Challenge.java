package com.example.LifeMaster_BE.Challenge;

import com.example.LifeMaster_BE.ETC.BaseEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challId;

    @Column
    private String challName;

    @Column
    private String challDesc;

    @Column
    private String challImg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private MemberEntity user;

    @Column
    @Builder.Default
    private Integer challCnt = 1;
}
