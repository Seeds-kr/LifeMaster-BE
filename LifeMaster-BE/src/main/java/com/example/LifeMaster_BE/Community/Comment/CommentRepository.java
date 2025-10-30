package com.example.LifeMaster_BE.Community.Comment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    void deleteByIdAndPostId(Long commentId, Long postId);

    Optional<CommentEntity> findByIdAndPostIdAndMemberId(Long commentId, Long postId, Long memberId);

    @EntityGraph(attributePaths = {"member"})
    List<CommentEntity> findByPostId(Long postId);

    @EntityGraph(attributePaths = {"post"})
    Optional<CommentEntity> findWithPostByIdAndPostIdAndMemberId(Long commentId, Long postId, Long memberId);

}
