package com.example.LifeMaster_BE.Group;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String icon; // 그룹 아이콘 (URL 또는 파일 경로)

    @Column(nullable = false)
    private String name; // 그룹명

    @Lob
    private String description; // 그룹 설명

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<GoalEntity> goals; // 그룹에 속한 목표들

    @ElementCollection
    @CollectionTable(name = "group_statistics", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "statistic")
    private List<String> statistics; // 통계 표시 항목 설정 (수면 시간, 공부 시간 등)

    private String password; // 비밀번호 (해싱 필요)

    // 생성자, getter, setter
    public GroupEntity() {}

    public GroupEntity(String icon, String name, String description, List<String> statistics, String password) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.statistics = statistics;
        this.password = password;
    }

    // 추가된 목표를 그룹에 추가하는 메소드
    public void addGoal(GoalEntity goal) {
        this.goals.add(goal);
        goal.setGroup(this); // 목표가 그룹을 참조하게 설정
    }
}
