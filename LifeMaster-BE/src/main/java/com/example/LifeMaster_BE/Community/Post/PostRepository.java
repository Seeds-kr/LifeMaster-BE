package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Admin.Dto.AdminPostResponseDto;
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
    @Query("""
            UPDATE PostEntity p
            SET p.viewCount = p.viewCount + 1
            WHERE p.id = :postId
            """)
    void increaseViewCount(@Param("postId") Long postId);

    List<PostEntity> findTop2ByOrderByViewCountDesc();

    /*
     * =========================
     * 관리자 게시글 관리
     * =========================
     */

    /**
     * 게시글 관리자용 페이징 조회.
     *
     * keyword:
     * - 게시글 제목
     * - 게시글 내용
     * - 게시자 이메일
     * - 게시자 닉네임
     *
     * postId, memberId:
     * - keyword가 숫자일 때 서비스에서 전달
     */
    @Query(
            value = """
                SELECT new com.example.LifeMaster_BE.Admin.Dto.AdminPostResponseDto(
                    p.id,
                    p.title,
                    CASE
                        WHEN LENGTH(p.content) > 100
                        THEN CONCAT(SUBSTRING(p.content, 1, 100), '...')
                        ELSE p.content
                    END,
                    p.file,
                    p.type,
                    p.calendarShared,
                    m.id,
                    m.email,
                    m.nickname,
                    p.viewCount,
                    p.commentCount,
                    COUNT(DISTINCT pl.id),
                    p.createdAt
                )
                FROM PostEntity p
                JOIN p.member m
                LEFT JOIN p.likes pl
                WHERE
                    (:type IS NULL OR p.type = :type)
                    AND (
                        :keyword IS NULL
                        OR :keyword = ''
                        OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(m.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR (:postId IS NOT NULL AND p.id = :postId)
                        OR (:memberId IS NOT NULL AND m.id = :memberId)
                    )
                GROUP BY
                    p.id,
                    p.title,
                    p.content,
                    p.file,
                    p.type,
                    p.calendarShared,
                    m.id,
                    m.email,
                    m.nickname,
                    p.viewCount,
                    p.commentCount,
                    p.createdAt
                """,
            countQuery = """
                SELECT COUNT(p)
                FROM PostEntity p
                JOIN p.member m
                WHERE
                    (:type IS NULL OR p.type = :type)
                    AND (
                        :keyword IS NULL
                        OR :keyword = ''
                        OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(m.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR (:postId IS NOT NULL AND p.id = :postId)
                        OR (:memberId IS NOT NULL AND m.id = :memberId)
                    )
                """
    )
    Page<AdminPostResponseDto> searchAdminPosts(
            @Param("keyword") String keyword,
            @Param("type") PostType type,
            @Param("postId") Long postId,
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    long countByType(PostType type);

    long countByCalendarSharedTrue();

    /*
     * =========================
     * 기존 관리자 기능
     * =========================
     */

    @EntityGraph(attributePaths = {"member"})
    Page<PostEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("""
            SELECT p
            FROM PostEntity p
            WHERE
                :keyword IS NULL
                OR p.title LIKE %:keyword%
                OR p.content LIKE %:keyword%
            """)
    Page<PostEntity> searchPosts(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(p)
            FROM PostEntity p
            WHERE p.createdAt >= :startDate
            """)
    long countNewPostsSince(@Param("startDate") LocalDateTime startDate);

    long countByMemberId(Long memberId);

    @Query("""
            SELECT new com.example.LifeMaster_BE.Admin.Dto.DailyCountDto(
                FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d'),
                COUNT(p)
            )
            FROM PostEntity p
            GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d')
            ORDER BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d')
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
    List<PopularPostDto> findPopularPosts(Pageable pageable);
}