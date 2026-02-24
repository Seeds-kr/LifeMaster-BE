package com.example.LifeMaster_BE.Admin;

import com.example.LifeMaster_BE.Admin.Dto.DashboardSummaryDto;
import com.example.LifeMaster_BE.Community.Comment.CommentRepository;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import com.example.LifeMaster_BE.Report.ReportRepository;
import com.example.LifeMaster_BE.Report.ReportStatus;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    public DashboardSummaryDto getDashboardSummary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();

        return DashboardSummaryDto.builder()
                // 회원 통계
                .totalMembers(memberRepository.count())
                .activeMembers(memberRepository.countByMemberStatus(MemberStatus.ACTIVE))
                .suspendedMembers(memberRepository.countByMemberStatus(MemberStatus.SUSPENDED))
                .newMembersToday(memberRepository.countNewMembersSince(todayStart))
                .newMembersThisWeek(memberRepository.countNewMembersSince(weekStart))
                // 콘텐츠 통계
                .totalPosts(postRepository.count())
                .newPostsToday(postRepository.countNewPostsSince(todayStart))
                .totalComments(commentRepository.count())
                .newCommentsToday(commentRepository.countNewCommentsSince(todayStart))
                // 신고 통계
                .totalReports(reportRepository.count())
                .pendingReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .reviewingReports(reportRepository.countByStatus(ReportStatus.REVIEWING))
                .resolvedReports(reportRepository.countByStatus(ReportStatus.RESOLVED))
                .build();
    }
}
