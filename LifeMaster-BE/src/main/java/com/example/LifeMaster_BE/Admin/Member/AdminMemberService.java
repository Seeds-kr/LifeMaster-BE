package com.example.LifeMaster_BE.Admin.Member;

import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
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
     * 전체 회원 페이징 조회
     */
    @Transactional(readOnly = true)
    public Page<AdminMemberDto> getAllMembers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return memberRepository.findAll(pageable)
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
}
