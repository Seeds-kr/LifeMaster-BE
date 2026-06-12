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

    private String name;

    @JsonProperty("goalCondition")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCondition goalCondition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalDuration duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalType goalType; // ✅ 추가

    private int value;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "FK_GROUP_ID"))
    @JsonBackReference
    private GroupEntity group;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public GoalEntity() {}

    public GoalEntity(GoalDTO goalDTO, GroupEntity group) {
        this.name = goalDTO.getName();
        this.goalCondition = goalDTO.getGoalCondition();
        this.value = goalDTO.getValue();
        this.duration = goalDTO.getDuration();
        this.goalType = goalDTO.getGoalType(); // ✅ 추가
        this.group = group;
    }
}