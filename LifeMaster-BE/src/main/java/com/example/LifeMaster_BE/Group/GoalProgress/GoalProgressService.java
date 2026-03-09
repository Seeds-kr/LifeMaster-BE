package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalDuration;
import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Group.GoalAchievement.GoalAchievementEntity;
import com.example.LifeMaster_BE.Group.GoalAchievement.GoalAchievementRepository;
import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.Group.GroupRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class GoalProgressService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final GoalProgressRepository goalProgressRepository;
    private final GoalRepository goalRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GoalAchievementRepository goalAchievementRepository;

    public GoalProgressService(
            GoalProgressRepository goalProgressRepository,
            GoalRepository goalRepository,
            GroupRepository groupRepository,
            MemberRepository memberRepository,
            GoalAchievementRepository goalAchievementRepository
    ) {
        this.goalProgressRepository = goalProgressRepository;
        this.goalRepository = goalRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.goalAchievementRepository = goalAchievementRepository;
    }

    /**
     * 그룹 목표 전체 진행률
     * 공식: (현재 기간 총 진행값 / (목표값 * 참여 유저 수)) * 100
     */
    public double calculateProgress(GoalEntity goal) {
        LocalDateTime startTime = getStartDateTimeForCurrentPeriod(goal.getDuration());

        List<GoalProgressEntity> progressList =
                goalProgressRepository.findByGoalAndSubmittedAtAfter(goal, startTime);

        long uniqueUserCount = progressList.stream()
                .map(progress -> progress.getUser().getId())
                .distinct()
                .count();

        if (uniqueUserCount == 0) {
            return 0.0;
        }

        int goalValue = goal.getValue();
        if (goalValue <= 0) {
            return 0.0;
        }

        int totalProgress = progressList.stream()
                .mapToInt(GoalProgressEntity::getProgressValue)
                .sum();

        double raw = (totalProgress / (double) (goalValue * uniqueUserCount)) * 100.0;
        return Math.min(raw, 100.0);
    }

    /**
     * 특정 유저의 현재 기간 목표 진행률
     * 공식: (현재 기간 유저 진행값 / 목표값) * 100
     */
    public double calculateUserProgress(GoalEntity goal, MemberEntity user) {
        LocalDateTime startTime = getStartDateTimeForCurrentPeriod(goal.getDuration());

        List<GoalProgressEntity> progressList =
                goalProgressRepository.findByGoalAndUserAndSubmittedAtAfter(goal, user, startTime);

        int totalProgress = progressList.stream()
                .mapToInt(GoalProgressEntity::getProgressValue)
                .sum();

        int goalValue = goal.getValue();
        if (goalValue <= 0) {
            return 0.0;
        }

        double raw = (totalProgress / (double) goalValue) * 100.0;
        return Math.min(raw, 100.0);
    }

    /**
     * 현재 캘린더 기간 시작 시각 반환
     * DAILY   : 오늘 00:00
     * WEEKLY  : 이번 주 월요일 00:00
     * MONTHLY : 이번 달 1일 00:00
     */
    private LocalDateTime getStartDateTimeForCurrentPeriod(GoalDuration duration) {
        LocalDate today = LocalDate.now(ZONE_ID);

        switch (duration) {
            case DAILY:
                return today.atStartOfDay();

            case WEEKLY:
                return today.with(DayOfWeek.MONDAY).atStartOfDay();

            case MONTHLY:
                return today.withDayOfMonth(1).atStartOfDay();

            default:
                throw new IllegalArgumentException("Invalid duration: " + duration);
        }
    }

    /**
     * 현재 캘린더 기간 시작 날짜 반환
     * achievement 중복 체크용
     */
    private LocalDate getPeriodStartDate(GoalDuration duration) {
        LocalDate today = LocalDate.now(ZONE_ID);

        switch (duration) {
            case DAILY:
                return today;

            case WEEKLY:
                return today.with(DayOfWeek.MONDAY);

            case MONTHLY:
                return today.withDayOfMonth(1);

            default:
                throw new IllegalArgumentException("Invalid duration: " + duration);
        }
    }

    /**
     * 목표 진행 기록 추가
     */
    @Transactional
    public GoalProgressEntity addGoalProgress(Long groupId, Long goalId, Long userId, int progressValue) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + goalId));

        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        GoalProgressEntity goalProgress = new GoalProgressEntity(user, group, goal, progressValue);
        GoalProgressEntity saved = goalProgressRepository.save(goalProgress);

        saveAchievementIfCompleted(group, goal, user);

        return saved;
    }

    /**
     * 현재 기간 내 100% 이상 달성 시 achievement 저장
     * 같은 기간 내 중복 저장 방지
     */
    private void saveAchievementIfCompleted(GroupEntity group, GoalEntity goal, MemberEntity user) {
        LocalDate periodStartDate = getPeriodStartDate(goal.getDuration());

        boolean alreadyAchieved = goalAchievementRepository
                .existsByGroupIdAndGoalIdAndUserIdAndPeriodStartDate(
                        group.getId(),
                        goal.getId(),
                        user.getId(),
                        periodStartDate
                );

        if (alreadyAchieved) {
            return;
        }

        LocalDateTime startTime = getStartDateTimeForCurrentPeriod(goal.getDuration());

        List<GoalProgressEntity> progressList =
                goalProgressRepository.findByGoalAndUserAndSubmittedAtAfter(goal, user, startTime);

        int totalProgress = progressList.stream()
                .mapToInt(GoalProgressEntity::getProgressValue)
                .sum();


        double userProgress = calculateUserProgress(goal, user);

        if (userProgress >= 100.0 && totalProgress >= goal.getValue()) {
            GoalAchievementEntity achievement = new GoalAchievementEntity(
                    group,
                    goal,
                    user,
                    periodStartDate,
                    LocalDateTime.now(ZONE_ID)
            );

            goalAchievementRepository.save(achievement);
        }
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
    @Transactional(readOnly = true)
    public List<GoalProgressResponseDTO> getAllGoalProgress() {
        return goalProgressRepository.findAllWithDetails()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 사용자 ID 기준 목표 진행 기록 조회
     */
    @Transactional(readOnly = true)
    public List<GoalProgressResponseDTO> getGoalProgressByUserId(Long userId) {
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return goalProgressRepository.findByUserWithDetails(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private GoalProgressResponseDTO toDto(GoalProgressEntity entity) {
        return new GoalProgressResponseDTO(
                entity.getId(),
                entity.getUser().getId(),
                entity.getUser().getNickname(),
                entity.getGroup().getId(),
                entity.getGoal().getId(),
                entity.getGoal().getName(),
                entity.getProgressValue(),
                entity.getSubmittedAt()
        );
    }

    /**
     * 그룹 삭제 시 진행 기록 삭제
     */
    public void deleteByGroupId(Long groupId) {
        goalProgressRepository.deleteByGroupId(groupId);
    }
}