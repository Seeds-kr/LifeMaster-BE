package com.example.LifeMaster_BE.Community.Vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollOptionRepository extends JpaRepository<VoteEntity.PollOption, Long> {
    List<VoteEntity.PollOption> findByPollId(Long pollId);
}
