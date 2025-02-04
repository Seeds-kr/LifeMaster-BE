package com.example.LifeMaster_BE.Group.GroupExit;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GroupExitHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    private LocalDateTime exitTime;

    public GroupExitHistoryEntity(GroupEntity group, MemberEntity member) {
        this.group = group;
        this.member = member;
        this.exitTime = LocalDateTime.now();
    }
}
