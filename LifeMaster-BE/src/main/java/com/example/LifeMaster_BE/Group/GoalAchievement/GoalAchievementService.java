package com.example.LifeMaster_BE.Group.GoalAchievement;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Group.GroupRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class GoalAchievementService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final GoalAchievementRepository goalAchievementRepository;
    private final GroupRepository groupRepository;
    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;

    private final SubscriptionAccessService subscriptionAccessService;

    public GoalAchievementService(
            GoalAchievementRepository goalAchievementRepository,
            GroupRepository groupRepository,
            GoalRepository goalRepository, MemberRepository memberRepository, SubscriptionAccessService subscriptionAccessService
    ) {
        this.goalAchievementRepository = goalAchievementRepository;
        this.groupRepository = groupRepository;
        this.goalRepository = goalRepository;
        this.memberRepository = memberRepository;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    public List<GoalAchievementHeatmapDto> getLast30DaysHeatmapByGroup(Long userId, Long groupId) {

        MemberEntity member = getMemberOrThrow(userId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found with id: " + groupId);
        }

        LocalDate today = LocalDate.now(ZONE_ID);
        LocalDate startDate = today.minusDays(29);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<GoalAchievementEntity> achievements =
                goalAchievementRepository.findByGroupIdAndAchievedAtBetweenOrderByAchievedAtAsc(
                        groupId, start, end
                );

        Map<LocalDate, Long> countMap = achievements.stream()
                .collect(Collectors.groupingBy(
                        achievement -> achievement.getAchievedAt().toLocalDate(),
                        Collectors.mapping(
                                achievement -> achievement.getUser().getId(),
                                Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size())
                        )
                ));

        List<GoalAchievementHeatmapDto> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            result.add(new GoalAchievementHeatmapDto(
                    date,
                    countMap.getOrDefault(date, 0L)
            ));
        }

        return result;
    }

    public List<GoalAchievementHeatmapDto> getLast30DaysHeatmapByGroupAndGoal(Long userId, Long groupId, Long goalId) {

        MemberEntity member = getMemberOrThrow(userId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found with id: " + groupId);
        }

        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with id: " + goalId));

        if (!goal.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Goal does not belong to the specified group.");
        }

        LocalDate today = LocalDate.now(ZONE_ID);
        LocalDate startDate = today.minusDays(29);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<GoalAchievementEntity> achievements =
                goalAchievementRepository.findByGroupIdAndGoalIdAndAchievedAtBetweenOrderByAchievedAtAsc(
                        groupId, goalId, start, end
                );

        Map<LocalDate, Long> countMap = achievements.stream()
                .collect(Collectors.groupingBy(
                        achievement -> achievement.getAchievedAt().toLocalDate(),
                        Collectors.mapping(
                                achievement -> achievement.getUser().getId(),
                                Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size())
                        )
                ));

        List<GoalAchievementHeatmapDto> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            result.add(new GoalAchievementHeatmapDto(
                    date,
                    countMap.getOrDefault(date, 0L)
            ));
        }

        return result;
    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}