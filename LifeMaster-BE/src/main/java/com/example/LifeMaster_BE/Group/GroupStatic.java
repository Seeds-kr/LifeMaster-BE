package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Challenge.Challenge;
import com.example.LifeMaster_BE.ETC.BaseEntity;
import com.example.LifeMaster_BE.TimeManager.Sleep.Sleep;
import com.example.LifeMaster_BE.TimeManager.Sleep.SleepRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GroupStatic extends BaseEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId")
    private GroupEntity group;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column
    private LocalDateTime averTime; // 평균 시간


}
