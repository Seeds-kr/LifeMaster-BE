package com.example.LifeMaster_BE.Challenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeCompletionRepository
        extends JpaRepository<ChallengeCompletion, Long> {

    boolean existsByUserIdAndChallenge_ChallIdAndDateKey(
            Long userId,
            Long challId,
            String dateKey
    );

    List<ChallengeCompletion> findByUser_IdAndDateKey(
            Long userId,
            String dateKey
    );
}