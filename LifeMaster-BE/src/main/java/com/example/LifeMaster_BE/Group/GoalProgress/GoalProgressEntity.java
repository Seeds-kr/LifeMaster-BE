package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.GroupEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class GoalProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail; // 목표를 수행한 유저의 이메일

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "FK_PROGRESS_GROUP_ID"))
    private GroupEntity group; // 그룹 참조

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false, foreignKey = @ForeignKey(name = "FK_PROGRESS_GOAL_ID"))
    private GoalEntity goal; // 목표 참조

    private int progressValue; // 수행 횟수 또는 시간

    @CreationTimestamp
    private LocalDateTime submittedAt; // 제출 시간

    // 생성자
    public GoalProgressEntity() {}

    public GoalProgressEntity(String userEmail, GroupEntity group, GoalEntity goal, int progressValue) {
        this.userEmail = userEmail;
        this.group = group;
        this.goal = goal;
        this.progressValue = progressValue;
    }
}

