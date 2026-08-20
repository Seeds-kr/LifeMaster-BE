package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Group.Goal.GoalDuration;
import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Group.Goal.GoalType;
import com.example.LifeMaster_BE.Group.GoalAchievement.GoalAchievementEntity;
import com.example.LifeMaster_BE.Group.GoalAchievement.GoalAchievementRepository;
import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.Group.GroupRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
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

    private final SleepGoalProgressService sleepGoalProgressService;
    private final PomodoroGoalProgressService pomodoroGoalProgressService;
    private final DetoxGoalProgressService detoxGoalProgressService;

    private final RepeatDetoxGoalProgressService repeatDetoxGoalProgressService;

    private final ChallengeGoalProgressService challengeGoalProgressService;

    private final GratitudeGoalProgressService gratitudeGoalProgressService;

    private final ReflectionGoalProgressService reflectionGoalProgressService;

    public GoalProgressService(
            GoalProgressRepository goalProgressRepository,
            GoalRepository goalRepository,
            GroupRepository groupRepository,
            MemberRepository memberRepository,
            GoalAchievementRepository goalAchievementRepository,
            SubscriptionAccessService subscriptionAccessService,
            SleepGoalProgressService sleepGoalProgressService,
            PomodoroGoalProgressService pomodoroGoalProgressService,
            DetoxGoalProgressService detoxGoalProgressService,
            RepeatDetoxGoalProgressService repeatDetoxGoalProgressService, ChallengeGoalProgressService challengeGoalProgressService, GratitudeGoalProgressService gratitudeGoalProgressService, ReflectionGoalProgressService reflectionGoalProgressService
    ) {
        this.goalProgressRepository = goalProgressRepository;
        this.goalRepository = goalRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.goalAchievementRepository = goalAchievementRepository;
        this.subscriptionAccessService = subscriptionAccessService;
        this.sleepGoalProgressService = sleepGoalProgressService;
        this.pomodoroGoalProgressService = pomodoroGoalProgressService;
        this.detoxGoalProgressService = detoxGoalProgressService;
        this.repeatDetoxGoalProgressService = repeatDetoxGoalProgressService;
        this.challengeGoalProgressService = challengeGoalProgressService;
        this.gratitudeGoalProgressService = gratitudeGoalProgressService;
        this.reflectionGoalProgressService = reflectionGoalProgressService;
    }

    @Transactional(readOnly = true)
    public double calculateProgress(Long userId, GoalEntity goal) {
        MemberEntity member = getMemberOrThrow(userId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        Collection<MemberEntity> members = goal.getGroup().getMembers();

        if (members == null || members.isEmpty() || goal.getValue() <= 0) {
            return 0.0;
        }

        double totalProgress;

        if (isAutomaticGoal(goal.getGoalType())) {
            totalProgress = members.stream()
                    .mapToDouble(user -> getActualProgress(goal, user))
                    .sum();
        } else {
            LocalDateTime startTime = getStartDateTimeForCurrentPeriod(goal.getDuration());

            totalProgress = goalProgressRepository
                    .findByGoalAndSubmittedAtAfter(goal, startTime)
                    .stream()
                    .mapToDouble(GoalProgressEntity::getProgressValue)
                    .sum();
        }

        double raw = (totalProgress / (goal.getValue() * (double) members.size())) * 100.0;
        return Math.min(raw, 100.0);
    }

    @Transactional(readOnly = true)
    public double calculateUserProgress(GoalEntity goal, MemberEntity user) {
        MemberEntity member = getMemberOrThrow(user.getId());
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        if (goal.getValue() <= 0) return 0.0;

        double totalProgress = getActualProgress(goal, member);
        return Math.min((totalProgress / goal.getValue()) * 100.0, 100.0);
    }

    private double getActualProgress(
            GoalEntity goal,
            MemberEntity user
    ) {
        LocalDate startDate =
                getPeriodStartDate(goal.getDuration());

        LocalDate endDate =
                getPeriodEndDate(goal.getDuration());

        return calculateActualProgress(
                goal,
                user,
                startDate,
                endDate
        );
    }

    private boolean isAutomaticGoal(GoalType goalType) {
        return goalType == GoalType.SLEEP
                || goalType == GoalType.POMODORO
                || goalType == GoalType.DETOX
                || goalType == GoalType.CHALLENGE
                || goalType == GoalType.GRATITUDE
                || goalType == GoalType.REFLECTION;
    }

    private LocalDateTime getStartDateTimeForCurrentPeriod(GoalDuration duration) {
        LocalDate today = LocalDate.now(ZONE_ID);

        return switch (duration) {
            case DAILY -> today.atStartOfDay();
            case WEEKLY -> today.with(DayOfWeek.MONDAY).atStartOfDay();
            case MONTHLY -> today.withDayOfMonth(1).atStartOfDay();
        };
    }

    private LocalDate getPeriodStartDate(GoalDuration duration) {
        LocalDate today = LocalDate.now(ZONE_ID);

        return switch (duration) {
            case DAILY -> today;
            case WEEKLY -> today.with(DayOfWeek.MONDAY);
            case MONTHLY -> today.withDayOfMonth(1);
        };
    }

    private LocalDate getPeriodEndDate(GoalDuration duration) {
        LocalDate today = LocalDate.now(ZONE_ID);

        return switch (duration) {
            case DAILY -> today;
            case WEEKLY -> today.with(DayOfWeek.SUNDAY);
            case MONTHLY -> today.withDayOfMonth(today.lengthOfMonth());
        };
    }

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

        if (isAutomaticGoal(goal.getGoalType())) {
            throw new IllegalArgumentException(
                    goal.getGoalType() + " goal progress is calculated automatically."
            );
        }

        GoalProgressEntity saved = goalProgressRepository.save(
                new GoalProgressEntity(user, group, goal, progressValue)
        );

        syncAchievement(group, goal, user);

        return saved;
    }

    @Transactional
    public void syncAchievement(Long userId, GoalEntity goal) {
        MemberEntity user = getMemberOrThrow(userId);
        GroupEntity group = goal.getGroup();

        if (!group.getMembers().contains(user)) return;

        syncAchievement(group, goal, user);
    }

    private void syncAchievement(GroupEntity group, GoalEntity goal, MemberEntity user) {
        LocalDate periodStartDate = getPeriodStartDate(goal.getDuration());

        boolean alreadyAchieved = goalAchievementRepository
                .existsByGroupIdAndGoalIdAndUserIdAndPeriodStartDate(
                        group.getId(),
                        goal.getId(),
                        user.getId(),
                        periodStartDate
                );

        if (goal.getValue() <= 0) return;

        double actualProgress = getActualProgress(goal, user);
        boolean completed = actualProgress >= goal.getValue();

        if (completed && !alreadyAchieved) {
            GoalAchievementEntity achievement = new GoalAchievementEntity(
                    group,
                    goal,
                    user,
                    periodStartDate,
                    LocalDateTime.now(ZONE_ID)
            );

            goalAchievementRepository.save(achievement);

        } else if (!completed && alreadyAchieved) {
            goalAchievementRepository
                    .deleteByGroupIdAndGoalIdAndUserIdAndPeriodStartDate(
                            group.getId(),
                            goal.getId(),
                            user.getId(),
                            periodStartDate
                    );
        }
    }

    @Transactional
    public void deleteGoalProgress(Long userId, Long progressId) {
        MemberEntity member = getMemberOrThrow(userId);
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        GoalProgressEntity progress = goalProgressRepository.findById(progressId)
                .orElseThrow(() ->
                        new RuntimeException("Goal progress not found with id: " + progressId));

        if (!progress.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can delete only your own goal progress.");
        }

        GroupEntity group = progress.getGroup();
        GoalEntity goal = progress.getGoal();
        MemberEntity user = progress.getUser();

        goalProgressRepository.delete(progress);
        goalProgressRepository.flush();

        syncAchievement(group, goal, user);
    }

    @Transactional(readOnly = true)
    public List<GoalProgressResponseDTO> getAllGoalProgress() {
        return goalProgressRepository.findAllWithDetails()
                .stream()
                .map(this::toDto)
                .toList();
    }

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

    public void deleteByGroupId(Long groupId) {
        goalProgressRepository.deleteByGroupId(groupId);
    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with ID: " + userId));
    }

    @Transactional(readOnly = true)
    public List<AdminGroupGoalProgressDTO> getGoalProgressByGroupId(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found with id: " + groupId);
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
            throw new IllegalArgumentException("Group not found with id: " + groupId);
        }

        return goalProgressRepository
                .findByGroup_Id(groupId, pageable)
                .map(this::toAdminDto);
    }

    private AdminGroupGoalProgressDTO toAdminDto(GoalProgressEntity progress) {
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

    @Transactional(readOnly = true)
    public double calculateActualProgress(
            GoalEntity goal,
            MemberEntity user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return switch (goal.getGoalType()) {

            case SLEEP ->
                    sleepGoalProgressService.calculateSleepHours(
                            user,
                            startDate,
                            endDate
                    );

            case POMODORO ->
                    pomodoroGoalProgressService.calculatePomodoroMinutes(
                            user,
                            startDate,
                            endDate
                    );

            case DETOX ->
                    detoxGoalProgressService.calculateDetoxMinutes(
                            user,
                            startDate,
                            endDate
                    )
                            +
                            repeatDetoxGoalProgressService.calculateDetoxMinutes(
                                    user,
                                    startDate,
                                    endDate
                            );

            case CHALLENGE ->
                    challengeGoalProgressService.calculateChallengeCount(
                            user,
                            startDate,
                            endDate
                    );

            case GRATITUDE ->
                    gratitudeGoalProgressService.calculateGratitudeCount(
                            user,
                            startDate,
                            endDate
                    );

            case REFLECTION ->
                    reflectionGoalProgressService.calculateReflectionCount(
                            user,
                            startDate,
                            endDate
                    );
        };
    }
}