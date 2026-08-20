package com.example.LifeMaster_BE.TimeManager.Sleep;

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
public class SleepRecordedEventListener {

    private final GoalRepository goalRepository;
    private final GoalProgressService goalProgressService;

    /**
     * Sleep 데이터 저장/수정 Transaction이
     * 성공적으로 Commit된 이후 실행
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSleepRecorded(
            SleepRecordedEvent event
    ) {

        Long userId = event.userId();

        /*
         * 사용자가 참여 중인 그룹에서
         * SLEEP 타입 목표만 조회
         */
        List<GoalEntity> sleepGoals =
                goalRepository.findGoalsByUserIdAndGoalType(
                        userId,
                        GoalType.SLEEP
                );

        /*
         * 각각의 SLEEP 목표 달성 여부 검사
         */
        for (GoalEntity goal : sleepGoals) {

            goalProgressService.syncAchievement(
                    userId,
                    goal
            );
        }
    }
}