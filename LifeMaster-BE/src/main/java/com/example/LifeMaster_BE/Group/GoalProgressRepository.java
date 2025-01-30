package com.example.LifeMaster_BE.Group;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface GoalProgressRepository extends JpaRepository<GoalProgressEntity, Long> {
    List<GoalProgressEntity> findByGoalAndSubmittedAtAfter(GoalEntity goal, LocalDateTime submittedAt);

    List<GoalProgressEntity> findByGoalAndUserEmailAndSubmittedAtAfter(GoalEntity goal, String userEmail, LocalDateTime startTime);

    List<GoalProgressEntity> findByUserEmail(String userEmail);

    List<GoalProgressEntity> findByGoal(GoalEntity goal);

    List<GoalProgressEntity> findByGroup(GroupEntity group);
}

