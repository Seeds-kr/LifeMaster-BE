package com.example.LifeMaster_BE.Group.GoalAchievement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GoalAchievementRepository extends JpaRepository<GoalAchievementEntity, Long> {

    List<GoalAchievementEntity> findByGroupIdAndAchievedAtBetweenOrderByAchievedAtAsc(
            Long groupId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<GoalAchievementEntity> findByGroupIdAndGoalIdAndAchievedAtBetweenOrderByAchievedAtAsc(
            Long groupId,
            Long goalId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByGroupIdAndGoalIdAndUserIdAndPeriodStartDate(
            Long groupId,
            Long goalId,
            Long userId,
            LocalDate periodStartDate
    );

    void deleteByGroupId(Long groupId);
    void deleteByGoalId(Long goalId);
    void deleteByUserId(Long userId);
}