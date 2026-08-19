package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.Group.Goal.*;
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
public class DetoxProgressChangedEventListener {

    private final GoalRepository goalRepository;
    private final GoalProgressService goalProgressService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(DetoxProgressChangedEvent event) {
        List<GoalEntity> goals = goalRepository.findGoalsByUserIdAndGoalType(
                event.userId(),
                GoalType.DETOX
        );

        for (GoalEntity goal : goals) {
            goalProgressService.syncAchievement(event.userId(), goal);
        }
    }
}