package com.example.LifeMaster_BE.Community.Comment.Like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    Optional<LikeEntity> findByMemberIdAndCommentId(Long memberId, Long commentId);
    void deleteByMemberIdAndCommentId(Long memberId, Long commentId);
    List<LikeEntity> findByMemberIdAndCommentIdIn(Long memberId, List<Long> commentIds);
}
