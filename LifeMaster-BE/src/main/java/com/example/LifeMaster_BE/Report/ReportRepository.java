package com.example.LifeMaster_BE.Report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}
