package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.Group.GroupRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GoalProgressService {
    private final GoalProgressRepository goalProgressRepository;
    private final GoalRepository goalRepository;

    private final GroupRepository groupRepository;

    public GoalProgressService(GoalProgressRepository goalProgressRepository, GoalRepository goalRepository, GroupRepository groupRepository) {
        this.goalProgressRepository = goalProgressRepository;
        this.goalRepository = goalRepository;
        this.groupRepository = groupRepository;
    }

    public double calculateProgress(GoalEntity goal) {
        LocalDateTime startTime = getStartTimeForPeriod(goal.getCreatedAt(), goal.getDuration());

        // 특정 목표에 대한 기간 내 진행 기록 조회
        List<GoalProgressEntity> progressList = goalProgressRepository.findByGoalAndSubmittedAtAfter(goal, startTime);

        // 목표를 수행한 고유 유저 수 계산
        long uniqueUserCount = progressList.stream()
                .map(GoalProgressEntity::getUserEmail)
                .distinct()
                .count();

        if (uniqueUserCount == 0) {
            return 0.0; // 참여한 유저가 없으면 진행률 0%
        }

        // 전체 유저 진행 값 합산
        int totalProgress = progressList.stream()
                .mapToInt(GoalProgressEntity::getProgressValue)
                .sum();

        // 목표 달성률 계산: (총 진행값 / (목표치 * 유저 수)) * 100
        return (totalProgress / (double) (goal.getValue() * uniqueUserCount)) * 100;
    }

    public double calculateUserProgress(GoalEntity goal, String userEmail) {
        LocalDateTime startTime = getStartTimeForPeriod(goal.getCreatedAt(), goal.getDuration());
        List<GoalProgressEntity> progressList = goalProgressRepository.findByGoalAndUserEmailAndSubmittedAtAfter(goal, userEmail, startTime);

        int totalProgress = progressList.stream().mapToInt(GoalProgressEntity::getProgressValue).sum();
        return (totalProgress / (double) goal.getValue()) * 100;
    }

    private LocalDateTime getStartTimeForPeriod(LocalDateTime goalCreatedAt, String period) {
        switch (period.toLowerCase()) {
            case "daily":
                return goalCreatedAt.truncatedTo(ChronoUnit.DAYS);
            case "weekly":
                return goalCreatedAt.minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
            case "monthly":
                return goalCreatedAt.minusMonths(1).truncatedTo(ChronoUnit.DAYS);
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }
    }

    public GoalProgressEntity addGoalProgress(Long groupId, Long goalId, String userEmail, int progressValue) {
        // 그룹 조회
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        // 목표 조회
        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + goalId));

        // 목표 수행 데이터 생성
        GoalProgressEntity goalProgress = new GoalProgressEntity(userEmail, group, goal, progressValue);

        // 저장
        return goalProgressRepository.save(goalProgress);
    }

    /**
     * 목표 진행 기록 삭제
     */
    public void deleteGoalProgress(Long progressId) {
        if (!goalProgressRepository.existsById(progressId)) {
            throw new RuntimeException("Goal progress not found with id: " + progressId);
        }
        goalProgressRepository.deleteById(progressId);
    }

    /**
     * 전체 목표 진행 기록 조회
     */
    public List<GoalProgressEntity> getAllGoalProgress() {
        return goalProgressRepository.findAll();
    }

    /**
     * 사용자 이메일별 목표 진행 기록 조회
     */
    public List<GoalProgressEntity> getGoalProgressByUserEmail(String userEmail) {
        return goalProgressRepository.findByUserEmail(userEmail);
    }
}