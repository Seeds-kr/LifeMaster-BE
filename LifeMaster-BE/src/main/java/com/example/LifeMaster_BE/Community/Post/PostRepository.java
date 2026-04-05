package com.example.LifeMaster_BE.Community.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    List<PostEntity> findByIdIn(List<Long> ids);

    @EntityGraph(attributePaths = {"member", "likes"})
    List<PostEntity> findByType(PostType type, Sort sort);

    // 페이지네이션 적용 조회
    @EntityGraph(attributePaths = {"member"})
    Page<PostEntity> findByType(PostType type, Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    Optional<PostEntity> findByIdAndMemberId(Long postId, Long memberId);

    @Modifying
    @Transactional
    @Query("UPDATE PostEntity p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void increaseViewCount(@Param("postId") Long postId);

    // 조회수 기준 인기글 상위 2개 가져오기
    List<PostEntity> findTop2ByOrderByViewCountDesc();

    // 관리자용 전체 조회 (페이징)
    @EntityGraph(attributePaths = {"member"})
    Page<PostEntity> findAll(Pageable pageable);

    // 관리자용 검색
    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM PostEntity p WHERE " +
            "(:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<PostEntity> searchPosts(@Param("keyword") String keyword, Pageable pageable);

    // 게시글별 좋아요 수 조회
    @Query("SELECT l.post.id, COUNT(l) FROM PostLikeEntity l WHERE l.post.id IN :postIds GROUP BY l.post.id")
    List<Object[]> countLikesByPostIds(@Param("postIds") List<Long> postIds);

    // 통계용 메서드
    @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.createdAt >= :startDate")
    long countNewPostsSince(@Param("startDate") LocalDateTime startDate);
}
