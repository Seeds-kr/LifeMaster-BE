package com.example.LifeMaster_BE.Challenge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    Page<Challenge> findByChallNameContaining(String name, Pageable pageable);

    List<Challenge> findTop5ByOrderByCreatedAtDesc();

    long countJoinedByMemberIdAndDate(Long memberId, LocalDate date);

    Optional<Challenge> findByChallIdAndUser_Id(Long challId, Long memberId);

    long countByUser_IdAndChallDate(Long memberId, LocalDate challDate);
}
