package com.example.LifeMaster_BE.Group.GoalAchievement;

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

    public GoalAchievementService(GoalAchievementRepository goalAchievementRepository) {
        this.goalAchievementRepository = goalAchievementRepository;
    }

    public List<GoalAchievementHeatmapDto> getLast30DaysHeatmapByGroup(Long groupId) {
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

    public List<GoalAchievementHeatmapDto> getLast30DaysHeatmapByGroupAndGoal(Long groupId, Long goalId) {
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
}