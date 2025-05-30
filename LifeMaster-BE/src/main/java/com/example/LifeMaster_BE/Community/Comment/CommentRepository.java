package com.example.LifeMaster_BE.Community.Comment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    void deleteByIdAndPostId(Long commentId, Long postId);
    Optional<CommentEntity> findByIdAndPostId(Long commentId, Long postId);
    List<CommentEntity> findByPostId(Long postId);

    @EntityGraph(attributePaths = {"post"})
    Optional<CommentEntity> findWithPostByIdAndPostId(Long commentId, Long postId);

}
