package com.example.LifeMaster_BE.Group.GroupChat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupChatRepository extends JpaRepository<GroupChatEntity, Long> {
    List<GroupChatEntity> findByGroupIdOrderByTimestampAsc(Long groupId);
}
