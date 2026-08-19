package com.example.LifeMaster_BE.Group.GoalProgress;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReflectionGoalProgressService {

    private final DiaryRepository diaryRepository;

    @Transactional(readOnly = true)
    public double calculateReflectionCount(
            MemberEntity user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return diaryRepository.countByMember_IdAndDiaryDateBetween(
                user.getId(),
                startDate,
                endDate
        );
    }
}