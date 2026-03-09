package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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

}