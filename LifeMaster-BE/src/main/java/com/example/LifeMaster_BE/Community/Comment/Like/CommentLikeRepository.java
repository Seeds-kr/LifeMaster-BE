package com.example.LifeMaster_BE.Community.Comment.Like;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLikeEntity, Long> {

    Optional<CommentLikeEntity> findByMemberIdAndCommentId(Long memberId, Long commentId);
    void deleteByMemberIdAndCommentId(Long memberId, Long commentId);

    @EntityGraph(attributePaths = "comment")
    List<CommentLikeEntity> findByMemberIdAndCommentIdIn(Long memberId, List<Long> commentIds);
}
