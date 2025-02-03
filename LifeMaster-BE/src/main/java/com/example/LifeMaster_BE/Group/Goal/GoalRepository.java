package com.example.LifeMaster_BE.Group.Goal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<GoalEntity, Long> {
    void deleteByGroupId(Long groupId);
}