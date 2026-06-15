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
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
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
    private final SubscriptionAccessService subscriptionAccessService;

    public GoalProgressService(
            GoalProgressRepository goalProgressRepository,
            GoalRepository goalRepository,
            GroupRepository groupRepository,
            MemberRepository memberRepository,
            GoalAchievementRepository goalAchievementRepository,
            SubscriptionAccessService subscriptionAccessService
    ) {
        this.goalProgressRepository = goalProgressRepository;
        this.goalRepository = goalRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.goalAchievementRepository = goalAchievementRepository;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    /**
     * 그룹 목표 전체 진행률
     * 공식: (현재 기간 총 진행값 / (목표값 * 참여 유저 수)) * 100
     */
    public double calculateProgress(Long userId, GoalEntity goal) {

        MemberEntity member = getMemberOrThrow(userId);

        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        LocalDateTime startTime = getStartDateTimeForCurrentPeriod(goal.getDuration());

        List<GoalProgressEntity> progressList =
                goalProgressRepository.findByGoalAndSubmittedAtAfter(goal, startTime);

        int groupMemberCount = goal.getGroup().getMembers().size();

        if (groupMemberCount == 0) {
            return 0.0;
        }

        int goalValue = goal.getValue();
        if (goalValue <= 0) {
            return 0.0;
        }

        double totalProgress = progressList.stream()
                .mapToDouble(GoalProgressEntity::getProgressValue)
                .sum();

        double raw = (totalProgress / (goalValue * (double) groupMemberCount)) * 100.0;
        return Math.min(raw, 100.0);
    }

    /**
     * 특정 유저의 현재 기간 목표 진행률
     * 공식: (현재 기간 유저 진행값 / 목표값) * 100
     */
    public double calculateUserProgress(GoalEntity goal, MemberEntity user) {

        MemberEntity member = getMemberOrThrow(user.getId());

        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        LocalDateTime startTime = getStartDateTimeForCurrentPeriod(goal.getDuration());

        List<GoalProgressEntity> progressList =
                goalProgressRepository.findByGoalAndUserAndSubmittedAtAfter(goal, user, startTime);

        double totalProgress = progressList.stream()
                .mapToDouble(GoalProgressEntity::getProgressValue)
                .sum();

        int goalValue = goal.getValue();
        if (goalValue <= 0) {
            return 0.0;
        }

        double raw = (totalProgress / goalValue) * 100.0;
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
    public GoalProgressEntity addGoalProgress(
            Long groupId,
            Long goalId,
            Long userId,
            double progressValue
    ) {

        MemberEntity member = getMemberOrThrow(userId);

        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + goalId));

        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (!group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is not a member of this group.");
        }

        if (!goal.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Goal does not belong to this group.");
        }

        if (progressValue <= 0) {
            throw new IllegalArgumentException("Progress value must be greater than 0.");
        }

        GoalProgressEntity goalProgress =
                new GoalProgressEntity(user, group, goal, progressValue);

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

        double totalProgress = progressList.stream()
                .mapToDouble(GoalProgressEntity::getProgressValue)
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
    public void deleteGoalProgress(Long userId, Long progressId) {

        MemberEntity member = getMemberOrThrow(userId);

        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

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

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    @Transactional(readOnly = true)
    public List<AdminGroupGoalProgressDTO> getGoalProgressByGroupId(Long groupId) {

        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException(
                    "Group not found with id: " + groupId
            );
        }

        return goalProgressRepository.findByGroupIdWithDetails(groupId)
                .stream()
                .map(progress -> new AdminGroupGoalProgressDTO(
                        progress.getId(),
                        progress.getUser().getId(),
                        progress.getUser().getEmail(),
                        progress.getUser().getNickname(),
                        progress.getGoal().getId(),
                        progress.getGoal().getName(),
                        progress.getGoal().getGoalType() == null
                                ? null
                                : progress.getGoal().getGoalType().name(),
                        progress.getGoal().getDuration() == null
                                ? null
                                : progress.getGoal().getDuration().name(),
                        progress.getProgressValue(),
                        progress.getSubmittedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AdminGroupGoalProgressDTO> getGoalProgressByGroupId(
            Long groupId,
            Pageable pageable
    ) {
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException(
                    "Group not found with id: " + groupId
            );
        }

        return goalProgressRepository
                .findByGroup_Id(groupId, pageable)
                .map(this::toAdminDto);
    }

    private AdminGroupGoalProgressDTO toAdminDto(
            GoalProgressEntity progress
    ) {
        return new AdminGroupGoalProgressDTO(
                progress.getId(),
                progress.getUser().getId(),
                progress.getUser().getEmail(),
                progress.getUser().getNickname(),
                progress.getGoal().getId(),
                progress.getGoal().getName(),
                progress.getGoal().getGoalType() == null
                        ? null
                        : progress.getGoal().getGoalType().name(),
                progress.getGoal().getDuration() == null
                        ? null
                        : progress.getGoal().getDuration().name(),
                progress.getProgressValue(),
                progress.getSubmittedAt()
        );
    }
}