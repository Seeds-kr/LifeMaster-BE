package com.example.LifeMaster_BE.Admin.Member;

import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberStatus;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final MemberRepository memberRepository;

    /**
     * 전체 회원 페이징 조회 + 검색
     */
    @Transactional(readOnly = true)
    public Page<AdminMemberDto> getAllMembers(
            int page,
            int size,
            String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size);

        String trimmedKeyword = null;
        Long memberId = null;

        if (keyword != null && !keyword.isBlank()) {
            trimmedKeyword = keyword.trim();

            try {
                memberId = Long.parseLong(trimmedKeyword);
            } catch (NumberFormatException ignored) {
                // 숫자가 아니면 회원 ID 검색은 제외
            }
        }

        return memberRepository
                .searchMembers(
                        trimmedKeyword,
                        memberId,
                        null,
                        pageable
                )
                .map(AdminMemberDto::from);
    }

    /**
     * 회원 관리 요약 정보
     */
    @Transactional(readOnly = true)
    public AdminMemberSummaryDto getMemberSummary() {
        long totalMemberCount = memberRepository.count();

        long activeMemberCount =
                memberRepository.countByMemberStatus(MemberStatus.ACTIVE);

        long premiumMemberCount =
                memberRepository.countBySubscriptionPlan(SubscriptionPlan.PREMIUM);

        long adminMemberCount =
                memberRepository.countByLoginRole(LoginRole.ADMIN);

        return new AdminMemberSummaryDto(
                totalMemberCount,
                activeMemberCount,
                premiumMemberCount,
                adminMemberCount
        );
    }

    @Transactional
    public void grantAdminRole(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다."));

        member.grantAdminRole();
    }

    @Transactional
    public void revokeAdminRole(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 멤버를 찾을 수 없습니다."));

        member.revokeAdminRole();
    }
}