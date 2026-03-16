package com.example.LifeMaster_BE.Group.Goal;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class GoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 목표 이름 (수면 시간, 공부 시간 등)
    // setGoalCondition 수동 추가
    @Setter
    @JsonProperty("goalCondition")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCondition goalCondition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalDuration duration;
    private int value; // 목표값 (예: 7시간, 50회 등)

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "FK_GROUP_ID"))
    @JsonBackReference
    private GroupEntity group; // GroupEntity 참조

    @CreationTimestamp
    private LocalDateTime createdAt; // 목표 생성 시간

    // 생성자, getter, setter
    public GoalEntity() {}

    public GoalEntity(GoalDTO goalDTO, GroupEntity group) {
        this.name = goalDTO.getName();
        this.goalCondition = goalDTO.getGoalCondition();
        this.value = goalDTO.getValue();
        this.duration = goalDTO.getDuration();
        this.group = group;
    }
}

