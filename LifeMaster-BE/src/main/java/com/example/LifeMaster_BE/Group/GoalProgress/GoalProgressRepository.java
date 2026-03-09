package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GoalProgressRepository extends JpaRepository<GoalProgressEntity, Long> {

    List<GoalProgressEntity> findByGoalAndSubmittedAtAfter(
            GoalEntity goal,
            LocalDateTime startTime
    );

    List<GoalProgressEntity> findByGoalAndUserAndSubmittedAtAfter(
            GoalEntity goal,
            MemberEntity user,
            LocalDateTime startTime
    );

    List<GoalProgressEntity> findByUser(MemberEntity user);

    void deleteByGroupId(Long groupId);
    void deleteByGoalId(Long goalId);
    void deleteByUserId(Long userId);

    List<GoalProgressEntity> findByGoal(GoalEntity goal);

    @Query("""
        select gp
        from GoalProgressEntity gp
        join fetch gp.user
        join fetch gp.group
        join fetch gp.goal
        order by gp.id desc
    """)
    List<GoalProgressEntity> findAllWithDetails();

    @Query("""
        select gp
        from GoalProgressEntity gp
        join fetch gp.user
        join fetch gp.group
        join fetch gp.goal
        where gp.user = :user
        order by gp.id desc
    """)
    List<GoalProgressEntity> findByUserWithDetails(@Param("user") MemberEntity user);

    @Query("""
        select gp.user.id, gp.user.nickname, gp.user.imageUrl, count(gp)
        from GoalProgressEntity gp
        where gp.group.id = :groupId
        group by gp.user.id, gp.user.nickname, gp.user.imageUrl
        order by count(gp) desc, gp.user.id asc
    """)
    List<Object[]> findGroupRankingTotal(@Param("groupId") Long groupId);

    @Query("""
        select gp.user.id, gp.user.nickname, gp.user.imageUrl, count(gp)
        from GoalProgressEntity gp
        where gp.group.id = :groupId
          and gp.submittedAt >= :startDateTime
          and gp.submittedAt < :endDateTime
        group by gp.user.id, gp.user.nickname, gp.user.imageUrl
        order by count(gp) desc, gp.user.id asc
    """)
    List<Object[]> findGroupRankingWeekly(
            @Param("groupId") Long groupId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}