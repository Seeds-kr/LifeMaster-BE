package com.example.LifeMaster_BE.Community.Vote;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserVoteRepository extends JpaRepository<UserVote, Long> {

    @Query("select uv.option.id from UserVote uv " +
            "where uv.poll.id = :pollId and uv.member.id = :memberId")
    Optional<Long> findMyOptionId(@Param("pollId") Long pollId,
                                  @Param("memberId") Long memberId);
}