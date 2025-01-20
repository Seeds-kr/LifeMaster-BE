package com.example.LifeMaster_BE.Group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "group_entity")
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String icon; // 그룹 아이콘 (URL 또는 파일 경로)

    @Column(nullable = false)
    private String name; // 그룹명

    @Lob
    private String description; // 그룹 설명

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
}
