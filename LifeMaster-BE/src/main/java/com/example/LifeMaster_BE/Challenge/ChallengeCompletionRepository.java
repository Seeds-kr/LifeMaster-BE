package com.example.LifeMaster_BE.Challenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeCompletionRepository extends JpaRepository<ChallengeCompletion, Long> {

    boolean existsByUserIdAndChallenge_ChallIdAndDateKey(
            Long userId, Long challId, String dateKey
    );

    Optional<ChallengeCompletion> findByUserIdAndChallenge_ChallIdAndDateKey(
            Long userId, Long challId, String dateKey
    );
}