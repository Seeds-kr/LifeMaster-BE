package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private List<GoalEntity> goals = new ArrayList<>(); // ✅ null 안전 초기화

    // 목표의 ID 목록을 관리(통계 표시용)
    @ElementCollection
    @CollectionTable(name = "group_statistics_goals", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "goal_id")
    private List<Long> statistics = new ArrayList<>(); // ✅ null 안전 초기화

    // Many-to-Many 관계 설정
    @ManyToMany(mappedBy = "groups", cascade = CascadeType.ALL)
    private Set<MemberEntity> members = new HashSet<>(); // ✅ null 안전 초기화

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false, foreignKey = @ForeignKey(name = "FK_GROUP_CREATOR"))
    private MemberEntity creator; // 그룹 생성자

    private String password; // 비밀번호(해싱 필요)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupAccessType accessType = GroupAccessType.PUBLIC;

    @Column(unique = true, length = 8)
    private String inviteCode;

    public GroupEntity() {}

    public GroupEntity(
            String icon,
            String name,
            String description,
            List<Long> statistics,
            String password,
            MemberEntity creator,
            GroupAccessType accessType
    ) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        if (statistics != null) this.statistics = new ArrayList<>(statistics);
        this.password = password;
        this.creator = creator;
        this.accessType = accessType;
    }
    // ===== 편의 메서드 =====
    public void addGoal(GoalEntity goal) {
        if (goal == null) return;
        this.goals.add(goal);
        goal.setGroup(this);
    }

    public void addMember(MemberEntity member) {
        if (member == null) return;
        this.members.add(member);
        member.getGroups().add(this);
    }

    public void removeMember(MemberEntity member) {
        if (member == null) return;
        this.members.remove(member);
        member.getGroups().remove(this);
    }

    public void addStatisticGoal(Long goalId) {
        if (goalId == null) return;
        this.statistics.add(goalId);
    }

    public void removeStatisticGoal(Long goalId) {
        if (goalId == null) return;
        this.statistics.remove(goalId);
    }

    @PrePersist
    @PreUpdate
    private void validatePasswordRule() {
        if (accessType == null) {
            accessType = GroupAccessType.PUBLIC; // 기본값 안전장치(원하면 제거 가능)
        }

        if (!accessType.requiresPassword()) {
            // PUBLIC 같은 케이스: 비번은 무조건 null
            this.password = null;
            return;
        }

        // PRIVATE / PASSWORD 등: 비번 필수(DB에는 해시가 들어있어야 함)
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(accessType + " group must have password.");
        }
    }
}


