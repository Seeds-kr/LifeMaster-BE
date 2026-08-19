package com.example.LifeMaster_BE.Group.GoalAchievement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        SELECT ga.user.id, ga.user.nickname, ga.user.imageUrl, COUNT(ga)
        FROM GoalAchievementEntity ga
        WHERE ga.group.id = :groupId
          AND ga.achievedAt >= :startDateTime
          AND ga.achievedAt < :endDateTime
        GROUP BY ga.user.id, ga.user.nickname, ga.user.imageUrl
        ORDER BY COUNT(ga) DESC, ga.user.nickname ASC
    """)
    List<Object[]> findGroupRankingWeekly(
            @Param("groupId") Long groupId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("""
        SELECT ga.user.id, ga.user.nickname, ga.user.imageUrl, COUNT(ga)
        FROM GoalAchievementEntity ga
        WHERE ga.group.id = :groupId
        GROUP BY ga.user.id, ga.user.nickname, ga.user.imageUrl
        ORDER BY COUNT(ga) DESC, ga.user.nickname ASC
    """)
    List<Object[]> findGroupRankingTotal(@Param("groupId") Long groupId);

    void deleteByGroupIdAndGoalIdAndUserIdAndPeriodStartDate(
            Long groupId,
            Long goalId,
            Long userId,
            LocalDate periodStartDate
    );
}