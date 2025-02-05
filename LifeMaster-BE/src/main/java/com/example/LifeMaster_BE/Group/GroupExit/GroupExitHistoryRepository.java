package com.example.LifeMaster_BE.Group.GroupExit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupExitHistoryRepository extends JpaRepository<GroupExitHistoryEntity, Long> {
    List<GroupExitHistoryEntity> findByGroupIdOrderByExitTimeDesc(Long groupId);

    void deleteByGroupId(Long groupId);
}
