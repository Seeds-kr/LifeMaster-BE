package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Challenge.Detox.RepeatDetoxSession;
import com.example.LifeMaster_BE.Challenge.Detox.RepeatDetoxSessionRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepeatDetoxGoalProgressService {

    private final RepeatDetoxSessionRepository sessionRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public double calculateDetoxMinutes(
            MemberEntity user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<RepeatDetoxSession> sessions =
                sessionRepository.findByMember_IdAndDateBetween(
                        user.getId(),
                        startDate,
                        endDate
                );

        return sessions.stream()
                .mapToDouble(this::calculateSessionMinutes)
                .sum();
    }

    private double calculateSessionMinutes(RepeatDetoxSession session) {
        if (session.getCompletedMinutes() != null) {
            return session.getCompletedMinutes();
        }

        LocalDateTime now = LocalDateTime.now(KST);

        LocalDateTime actualEnd = now.isBefore(session.getScheduledEndAt())
                ? now
                : session.getScheduledEndAt();

        return Math.max(
                0,
                Duration.between(session.getStartedAt(), actualEnd).toMinutes()
        );
    }
}