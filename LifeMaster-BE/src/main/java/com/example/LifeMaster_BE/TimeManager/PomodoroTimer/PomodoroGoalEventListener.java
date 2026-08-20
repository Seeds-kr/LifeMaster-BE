package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Group.Goal.GoalType;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PomodoroGoalEventListener {

    private final GoalRepository goalRepository;
    private final GoalProgressService goalProgressService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePomodoroCompleted(
            PomodoroCompletedEvent event
    ) {

        List<GoalEntity> pomodoroGoals =
                goalRepository.findGoalsByUserIdAndGoalType(
                        event.userId(),
                        GoalType.POMODORO
                );

        for (GoalEntity goal : pomodoroGoals) {

            goalProgressService.syncAchievement(
                    event.userId(),
                    goal
            );
        }
    }
}