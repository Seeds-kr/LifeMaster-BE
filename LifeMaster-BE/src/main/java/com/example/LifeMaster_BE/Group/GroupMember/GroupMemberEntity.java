package com.example.LifeMaster_BE.Group.GroupMember;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_member_group_user", columnNames = {"group_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_group_member_group_id", columnList = "group_id"),
                @Index(name = "idx_group_member_user_id", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 ID (MemberEntity FK를 안 걸고 id만 들고 가는 최소형)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 그룹 ID (GroupEntity FK를 안 걸고 id만 들고 가는 최소형)
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private GroupMemberRole role;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public void changeRole(GroupMemberRole role) {
        this.role = role;
    }
}
