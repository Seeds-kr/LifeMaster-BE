package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Setter
public class GoalProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MemberEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "group_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_PROGRESS_GROUP_ID")
    )
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "goal_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_PROGRESS_GOAL_ID")
    )
    private GoalEntity goal;

    @Column(nullable = false)
    private double progressValue;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    public GoalProgressEntity() {
    }

    public GoalProgressEntity(MemberEntity user, GroupEntity group, GoalEntity goal, double progressValue) {
        this.user = user;
        this.group = group;
        this.goal = goal;
        this.progressValue = progressValue;
    }

    @PrePersist
    public void prePersist() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
    }
}