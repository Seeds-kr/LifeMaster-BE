package com.example.LifeMaster_BE.Community.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    void deleteByIdAndPostId(Long commentId, Long postId);

    Optional<CommentEntity> findByIdAndPostIdAndMemberId(Long commentId, Long postId, Long memberId);

    @EntityGraph(attributePaths = {"member"})
    List<CommentEntity> findByPostId(Long postId);

    @EntityGraph(attributePaths = {"post"})
    Optional<CommentEntity> findWithPostByIdAndPostIdAndMemberId(Long commentId, Long postId, Long memberId);

    // 관리자용 전체 조회 (페이징)
    @EntityGraph(attributePaths = {"member", "post"})
    Page<CommentEntity> findAll(Pageable pageable);

    // 관리자용 검색
    @EntityGraph(attributePaths = {"member", "post"})
    @Query("SELECT c FROM CommentEntity c WHERE " +
            "(:keyword IS NULL OR c.comment LIKE %:keyword%)")
    Page<CommentEntity> searchComments(@Param("keyword") String keyword, Pageable pageable);

    // 통계용 메서드
    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.createdAt >= :startDate")
    long countNewCommentsSince(@Param("startDate") LocalDateTime startDate);
}
