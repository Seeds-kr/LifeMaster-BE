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

import com.example.LifeMaster_BE.Admin.Dto.UserStatisticsResponse;
import com.example.LifeMaster_BE.Challenge.ChallengeUserRepository;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final ChallengeUserRepository challengeUserRepository;
    private final TodoRepository todoRepository;

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

    public List<UserStatisticsResponse> getUserStatistics() {

        List<MemberEntity> members = memberRepository.findAll();

        return members.stream()
                .map(member -> {

                    Long challengeCount =
                            challengeUserRepository.countByUserId(member.getId());

                    Long groupCount =
                            (long) member.getGroups().size();

                    Long postCount =
                            postRepository.countByMemberId(member.getId());

                    Long commentCount =
                            commentRepository.countByMemberId(member.getId());

                    Long todoCount =
                            todoRepository.countByMemberId(member.getId());

                    Long score =
                            challengeCount +
                                    groupCount +
                                    postCount +
                                    commentCount +
                                    todoCount;

                    return new UserStatisticsResponse(
                            member.getId(),
                            member.getNickname(),
                            member.getEmail(),
                            challengeCount,
                            groupCount,
                            postCount,
                            commentCount,
                            todoCount,
                            score
                    );
                })
                .collect(Collectors.toList());
    }

    public UserStatisticsResponse getUserStatisticsDetail(Long userId) {

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        Long challengeCount =
                challengeUserRepository.countByUserId(member.getId());

        Long groupCount =
                (long) member.getGroups().size();

        Long postCount =
                postRepository.countByMemberId(member.getId());

        Long commentCount =
                commentRepository.countByMemberId(member.getId());

        Long todoCount =
                todoRepository.countByMemberId(member.getId());

        Long score =
                challengeCount +
                        groupCount +
                        postCount +
                        commentCount +
                        todoCount;

        return new UserStatisticsResponse(
                member.getId(),
                member.getNickname(),
                member.getEmail(),
                challengeCount,
                groupCount,
                postCount,
                commentCount,
                todoCount,
                score
        );
    }
}
