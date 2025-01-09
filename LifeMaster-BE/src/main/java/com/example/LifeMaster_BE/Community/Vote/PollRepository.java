package com.example.LifeMaster_BE.Community.Vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<VoteEntity.Poll, Long> {
    Optional<VoteEntity.Poll> findByTitle(String title);
}
