package com.example.LifeMaster_BE.Community.Vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<VoteEntity.Vote, Long> {
    boolean existsByPollIdAndUserId(Long pollId, String userId);
}
