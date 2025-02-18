package com.example.LifeMaster_BE.Challenge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    Page<Challenge> findByChallNameContaining(String name, Pageable pageable);

    List<Challenge> findTop5ByOrderByCreatedAtDesc();
}
