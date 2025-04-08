package com.example.LifeMaster_BE.Community.Post;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByType(PostType type, Sort sort);

    @EntityGraph(attributePaths = {"member"})
    Optional<PostEntity> findByIdAndMemberId(Long postId, Long memberId);

    @Modifying
    @Transactional
    @Query("UPDATE PostEntity p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void increaseViewCount(@Param("postId") Long postId);

    // 조회수 기준 인기글 상위 2개 가져오기
    List<PostEntity> findTop2ByOrderByViewCountDesc();
}
