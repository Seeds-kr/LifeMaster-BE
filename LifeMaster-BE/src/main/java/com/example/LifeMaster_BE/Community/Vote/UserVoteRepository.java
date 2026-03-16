package com.example.LifeMaster_BE.Community.Vote;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserVoteRepository extends JpaRepository<UserVote, Long> {

    // ✔ 현재 로그인한 유저가 투표했는지 여부
    boolean existsByPoll_IdAndMember_Id(Long pollId, Long memberId);

    // ✔ 이미 투표한 UserVote 엔티티 조회
    Optional<UserVote> findByPoll_IdAndMember_Id(Long pollId, Long memberId);

    // ✔ 선택한 옵션 ID 조회
    @Query("select uv.option.id from UserVote uv " +
            "where uv.poll.id = :pollId and uv.member.id = :memberId")
    Optional<Long> findMyOptionId(
            @Param("pollId") Long pollId,
            @Param("memberId") Long memberId
    );

    // 특정 투표에 대한 유저 투표 전부 삭제
    void deleteByPoll_Id(Long pollId);
}

