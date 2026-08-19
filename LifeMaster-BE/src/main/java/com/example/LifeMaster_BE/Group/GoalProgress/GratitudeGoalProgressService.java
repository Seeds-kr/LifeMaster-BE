package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GratitudeGoalProgressService {

    private final ThankRepository thankRepository;

    @Transactional(readOnly = true)
    public double calculateGratitudeCount(
            MemberEntity user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return thankRepository.countByMember_IdAndThankDateBetween(
                user.getId(),
                startDate,
                endDate
        );
    }
}