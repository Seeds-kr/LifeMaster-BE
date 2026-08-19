package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.Challenge.ChallengeCompletionRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ChallengeGoalProgressService {

    private final ChallengeCompletionRepository challengeCompletionRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional(readOnly = true)
    public double calculateChallengeCount(
            MemberEntity user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        String start = startDate.format(DATE_FORMATTER);
        String end = endDate.format(DATE_FORMATTER);

        return challengeCompletionRepository
                .findByUser_IdAndDateKeyBetween(user.getId(), start, end)
                .size();
    }
}