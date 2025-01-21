package com.example.LifeMaster_BE.Group;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class GoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 목표 이름 (수면 시간, 공부 시간 등)
    private String goal_condition; // 목표 조건 (예: N회 이상, N분 이상)
    private int value; // 목표값 (예: 7시간, 50회 등)

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "FK_GROUP_ID"))
    @JsonBackReference
    private GroupEntity group; // GroupEntity 참조

    // 생성자, getter, setter
    public GoalEntity() {}

    public GoalEntity(GoalDTO goalDTO, GroupEntity group) {
        this.name = goalDTO.getName();
        this.goal_condition = goalDTO.getGoalCondition();
        this.value = goalDTO.getValue();
        this.group = group;
    }

    // setGoalCondition 수동 추가
    public void setGoalCondition(String goalCondition) {
        this.goal_condition = goalCondition;
    }
}
