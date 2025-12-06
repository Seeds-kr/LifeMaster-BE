package com.example.LifeMaster_BE.Community.Vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollOptionRepository extends JpaRepository<VoteEntity.PollOption, Long> {
    List<VoteEntity.PollOption> findByPollId(Long pollId);

    // 특정 투표(poll)에 속한 옵션 전부 삭제
    void deleteByPoll_Id(Long pollId);
}
