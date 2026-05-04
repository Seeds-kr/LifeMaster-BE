package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Admin.Dto.DailyCountDto;
import com.example.LifeMaster_BE.Admin.Dto.PopularPostDto;
import com.example.LifeMaster_BE.Admin.Dto.PostTypeCountDto;
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

    // 통계용 메서드
    @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.createdAt >= :startDate")
    long countNewPostsSince(@Param("startDate") LocalDateTime startDate);

    long countByMemberId(Long memberId);

    @Query("""
        SELECT new com.example.LifeMaster_BE.Admin.Dto.DailyCountDto(
            FUNCTION('DATE', p.createdAt),
            COUNT(p)
        )
        FROM PostEntity p
        GROUP BY FUNCTION('DATE', p.createdAt)
        ORDER BY FUNCTION('DATE', p.createdAt)
    """)
    List<DailyCountDto> countDailyPosts();

    @Query("""
    SELECT new com.example.LifeMaster_BE.Admin.Dto.PostTypeCountDto(
        p.type,
        COUNT(p)
    )
    FROM PostEntity p
    GROUP BY p.type
""")
    List<PostTypeCountDto> countByPostType();

    @Query("""
    SELECT new com.example.LifeMaster_BE.Admin.Dto.PopularPostDto(
        p.id,
        p.title,
        p.viewCount,
        p.commentCount,
        SIZE(p.likes),
        (p.viewCount + p.commentCount + SIZE(p.likes) * 2)
    )
    FROM PostEntity p
    ORDER BY (p.viewCount + p.commentCount + SIZE(p.likes) * 2) DESC
""")
    List<PopularPostDto> findPopularPosts(org.springframework.data.domain.Pageable pageable);
}
