package com.example.LifeMaster_BE.Report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    // 관리자용 상태별 필터 조회
    @EntityGraph(attributePaths = {"member", "post"})
    Page<ReportEntity> findByStatus(ReportStatus status, Pageable pageable);

    // 관리자용 전체 조회 (페이징)
    @EntityGraph(attributePaths = {"member", "post"})
    Page<ReportEntity> findAll(Pageable pageable);

    // 상세 조회
    @EntityGraph(attributePaths = {"member", "post", "resolvedBy"})
    @Query("SELECT r FROM ReportEntity r WHERE r.id = :id")
    ReportEntity findDetailById(@Param("id") Long id);

    // 통계용 메서드
    long countByStatus(ReportStatus status);
}
