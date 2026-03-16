package com.example.LifeMaster_BE.Group.GoalAchievement;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "goal_achievement",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_goal_achievement_group_goal_user_period",
                        columnNames = {"group_id", "goal_id", "user_id", "period_start_date"}
                )
        },
        indexes = {
                @Index(name = "idx_achievement_group_achieved_at", columnList = "group_id, achieved_at"),
                @Index(name = "idx_achievement_group_goal_achieved_at", columnList = "group_id, goal_id, achieved_at"),
                @Index(name = "idx_achievement_user_achieved_at", columnList = "user_id, achieved_at")
        }
)
@Getter
@NoArgsConstructor
public class GoalAchievementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "group_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_ACHIEVEMENT_GROUP_ID")
    )
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "goal_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_ACHIEVEMENT_GOAL_ID")
    )
    private GoalEntity goal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_ACHIEVEMENT_USER_ID")
    )
    private MemberEntity user;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "achieved_at", nullable = false)
    private LocalDateTime achievedAt;

    public GoalAchievementEntity(
            GroupEntity group,
            GoalEntity goal,
            MemberEntity user,
            LocalDate periodStartDate,
            LocalDateTime achievedAt
    ) {
        this.group = group;
        this.goal = goal;
        this.user = user;
        this.periodStartDate = periodStartDate;
        this.achievedAt = achievedAt;
    }
}