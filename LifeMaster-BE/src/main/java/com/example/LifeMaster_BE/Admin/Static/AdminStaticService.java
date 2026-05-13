package com.example.LifeMaster_BE.Admin.Static;

import com.example.LifeMaster_BE.Admin.Dto.ActiveUserDto;
import com.example.LifeMaster_BE.Admin.Dto.DailyCountDto;
import com.example.LifeMaster_BE.Admin.Dto.MonthlyCountDto;
import com.example.LifeMaster_BE.Admin.Dto.*;
import com.example.LifeMaster_BE.Challenge.ChallengeUserRepository;
import com.example.LifeMaster_BE.Community.Comment.CommentRepository;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStaticService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChallengeUserRepository challengeUserRepository;

    // ======================
    // 회원 통계
    // ======================

    public List<DailyCountDto> getDailyUserStats() {
        return memberRepository.countDailyUsers(); // 👉 쿼리 직접 작성 필요
    }

    public List<MonthlyCountDto> getMonthlyUserStats() {
        return memberRepository.countMonthlyUsers();
    }

    // ======================
    // 콘텐츠 통계
    // ======================

    public List<DailyCountDto> getDailyPostStats() {
        return postRepository.countDailyPosts();
    }

    public List<DailyCountDto> getDailyCommentStats() {
        return commentRepository.countDailyComments();
    }

    // ======================
    // 활동 통계
    // ======================

    public ActiveUserDto getActiveUsers() {
        Long activeUsers = memberRepository.countActiveUsers(); // 기준 정의 필요
        return new ActiveUserDto(activeUsers);
    }

    public List<LoginTypeCountDto> getLoginTypeStats() {
        return memberRepository.countByLoginType();
    }

    public List<PostTypeCountDto> getPostTypeStats() {
        return postRepository.countByPostType();
    }

    public List<PopularPostDto> getPopularPosts() {
        return postRepository.findPopularPosts(
                org.springframework.data.domain.PageRequest.of(0, 10)
        );
    }

    public ChallengeCompletionDto getChallengeCompletion() {

        Long total = challengeUserRepository.countTotalParticipants();

        // ❗ 완료 개념 없음
        Long completed = total;

        double rate = total == 0 ? 0 : 100.0;

        return new ChallengeCompletionDto(total, completed, rate);
    }
}