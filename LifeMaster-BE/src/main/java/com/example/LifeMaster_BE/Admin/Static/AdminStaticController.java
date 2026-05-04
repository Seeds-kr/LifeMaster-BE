package com.example.LifeMaster_BE.Admin.Static;

import com.example.LifeMaster_BE.Admin.Dto.*;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Statistics", description = "관리자 통계 API")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStaticController {

    private final AdminStaticService adminStaticService;
    private final MemberRepository memberRepository;

    // ======================
    // 회원 통계
    // ======================

    @Operation(summary = "일별 가입자 수")
    @GetMapping("/users/daily")
    public List<DailyCountDto> getDailyUsers(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getDailyUserStats();
    }

    @Operation(summary = "월별 가입자 수")
    @GetMapping("/users/monthly")
    public List<MonthlyCountDto> getMonthlyUsers(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getMonthlyUserStats();
    }

    // ======================
    // 콘텐츠 통계
    // ======================

    @Operation(summary = "일별 게시글 수")
    @GetMapping("/posts/daily")
    public List<DailyCountDto> getDailyPosts(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getDailyPostStats();
    }

    @Operation(summary = "일별 댓글 수")
    @GetMapping("/comments/daily")
    public List<DailyCountDto> getDailyComments(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getDailyCommentStats();
    }

    // ======================
    // 활동 통계
    // ======================

    @Operation(summary = "활성 사용자 수")
    @GetMapping("/active-users")
    public ActiveUserDto getActiveUsers(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getActiveUsers();
    }

    private void validateAdmin(CustomUserDetails user) {
        MemberEntity member = memberRepository.findById(user.getId()).orElseThrow();
        if(member.getLoginRole() != LoginRole.ADMIN) {
            throw new RuntimeException("관리자만 접근 가능합니다.");
        }
    }

    // ======================
// 회원 통계
// ======================

    @Operation(summary = "로그인 타입별 통계")
    @GetMapping("/users/login-type")
    public List<LoginTypeCountDto> getLoginTypeStats(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getLoginTypeStats();
    }


// ======================
// 콘텐츠 통계
// ======================

    @Operation(summary = "게시글 타입별 통계")
    @GetMapping("/posts/type")
    public List<PostTypeCountDto> getPostTypeStats(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getPostTypeStats();
    }

    @Operation(summary = "인기 게시글 TOP 10")
    @GetMapping("/popular-posts")
    public List<PopularPostDto> getPopularPosts(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getPopularPosts();
    }


// ======================
// 활동 통계
// ======================

    @Operation(summary = "챌린지 완료율")
    @GetMapping("/challenge/completion")
    public ChallengeCompletionDto getChallengeCompletion(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        validateAdmin(user);
        return adminStaticService.getChallengeCompletion();
    }


}