package com.example.LifeMaster_BE.Group.GroupChat;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private MemberEntity sender;

    @Column(nullable = false)
    private String message;

    private LocalDateTime timestamp;

    // 읽음 처리
    @ManyToMany
    @JoinTable(
            name = "group_chat_read",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "reader_id")
    )
    private Set<MemberEntity> readBy = new HashSet<>();
}
