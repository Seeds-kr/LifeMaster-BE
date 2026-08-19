package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Challenge.Detox.*;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DetoxGoalProgressService {

    private final TimeDetoxRepository timeDetoxRepository;
    private final TimeDetoxDailyDisableRepository dailyDisableRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public double calculateDetoxMinutes(
            MemberEntity user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<TimeDetoxEntity> schedules =
                timeDetoxRepository.findAllByMember_Id(user.getId());

        double totalMinutes = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            for (TimeDetoxEntity schedule : schedules) {
                if (!isScheduleDay(schedule, date)) continue;

                totalMinutes += calculateActualMinutes(user.getId(), schedule, date);
            }
        }

        return totalMinutes;
    }

    private long calculateActualMinutes(
            Long memberId,
            TimeDetoxEntity schedule,
            LocalDate date
    ) {
        LocalDate today = LocalDate.now(KST);

        // 미래 날짜는 아직 수행하지 않았으므로 0
        if (date.isAfter(today)) return 0;

        LocalTime start = schedule.getStartTime();
        LocalTime end = schedule.getEndTime();

        if (start == null || end == null || !end.isAfter(start)) return 0;

        Optional<TimeDetoxDailyDisableEntity> disabled =
                dailyDisableRepository.findByMember_IdAndTimeDetox_IdAndDisabledDate(
                        memberId,
                        schedule.getId(),
                        date
                );

        // 비상탈출한 날
        if (disabled.isPresent()) {
            LocalTime escapeTime = disabled.get().getDisabledAt();

            if (escapeTime == null || !escapeTime.isAfter(start)) return 0;

            LocalTime actualEnd = escapeTime.isAfter(end) ? end : escapeTime;

            return ChronoUnit.MINUTES.between(start, actualEnd);
        }

        // 과거 날짜이고 비상탈출하지 않았다면 전체 수행
        if (date.isBefore(today)) {
            return ChronoUnit.MINUTES.between(start, end);
        }

        // 오늘
        LocalTime now = LocalTime.now(KST);

        if (now.isBefore(start)) return 0;

        if (!now.isBefore(end)) {
            return ChronoUnit.MINUTES.between(start, end);
        }

        // 현재 디톡스 진행 중
        return ChronoUnit.MINUTES.between(start, now);
    }

    private boolean isScheduleDay(TimeDetoxEntity schedule, LocalDate date) {
        if (!schedule.isActive()) return false;

        if (schedule.getDay() == null ||
                !schedule.getDay().equalsIgnoreCase(date.getDayOfWeek().name())) {
            return false;
        }

        if ("BIWEEKLY".equalsIgnoreCase(schedule.getCycle())) {
            LocalDate createdDate = schedule.getCreatedDate();
            if (createdDate == null) return true;

            long weeks = ChronoUnit.WEEKS.between(
                    createdDate.with(DayOfWeek.MONDAY),
                    date.with(DayOfWeek.MONDAY)
            );

            return weeks % 2 == 0;
        }

        return true;
    }
}