package com.example.LifeMaster_BE.Group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "goal_entity")
public class GoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 목표 이름 (수면 시간, 공부 시간 등)
    private String condition; // 목표 조건 (예: N회 이상, N분 이상)
    private int value; // 목표값 (예: 7시간, 50회 등)

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false) // 외래 키 설정
    private GroupEntity group; // GroupEntity 참조

    // 생성자, getter, setter
    public GoalEntity() {}

    public GoalEntity(String name, String condition, int value, GroupEntity group) {
        this.name = name;
        this.condition = condition;
        this.value = value;
        this.group = group;
    }
}
