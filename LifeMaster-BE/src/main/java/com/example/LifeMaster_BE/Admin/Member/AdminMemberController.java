package com.example.LifeMaster_BE.Admin.Member;

import com.example.LifeMaster_BE.UserManager.Member.Subscription.MemberSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    private final MemberSubscriptionService memberSubscriptionService;

    /**
     * 전체 회원 조회
     */
    @GetMapping
    public ResponseEntity<Page<AdminMemberDto>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AdminMemberDto> result =
                adminMemberService.getAllMembers(page, size);

        return ResponseEntity.ok(result);
    }

    /**
     * 회원 관리 상단 요약 정보
     */
    @GetMapping("/summary")
    public ResponseEntity<AdminMemberSummaryDto> getMemberSummary() {
        return ResponseEntity.ok(
                adminMemberService.getMemberSummary()
        );
    }

    /**
     * 관리자 권한 부여
     */
    @PatchMapping("/{memberId}/admin-role/grant")
    public ResponseEntity<Void> grantAdminRole(
            @PathVariable Long memberId
    ) {
        adminMemberService.grantAdminRole(memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * 관리자 권한 회수
     */
    @PatchMapping("/{memberId}/admin-role/revoke")
    public ResponseEntity<Void> revokeAdminRole(
            @PathVariable Long memberId
    ) {
        adminMemberService.revokeAdminRole(memberId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{memberId}/subscription/extend-one-month")
    public ResponseEntity<String> extendSubscriptionOneMonth(
            @PathVariable Long memberId
    ) {
        memberSubscriptionService.extendPremiumOneMonthForAdmin(memberId);
        return ResponseEntity.ok("구독 기한이 1개월 연장되었습니다.");
    }

    @PatchMapping("/{memberId}/subscription/revoke")
    public ResponseEntity<String> revokeSubscription(
            @PathVariable Long memberId
    ) {
        memberSubscriptionService.revokePremiumForAdmin(memberId);
        return ResponseEntity.ok("구독이 회수되었습니다.");
    }
}
