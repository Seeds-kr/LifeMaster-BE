package com.example.LifeMaster_BE.Admin.Premium;

import com.example.LifeMaster_BE.UserManager.Member.Subscription.MemberSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/premium")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final MemberSubscriptionService memberSubscriptionService;

    /**
     * 전체 프리미엄 유저 조회
     */
    @GetMapping("/premium-members")
    public ResponseEntity<List<PremiumMemberAdminDto>> getAllPremiumMembers() {
        List<PremiumMemberAdminDto> result =
                memberSubscriptionService.getAllPremiumMembersForAdmin();

        return ResponseEntity.ok(result);
    }

    /**
     * 일반 회원 조회 - 페이징
     */
    @GetMapping("/non-premium-members")
    public ResponseEntity<Page<NonPremiumMemberAdminDto>> getNonPremiumMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NonPremiumMemberAdminDto> result =
                memberSubscriptionService.getNonPremiumMembersForAdmin(page, size);

        return ResponseEntity.ok(result);
    }

    /**
     * 관리자 프리미엄 권한 부여
     */
    @PostMapping("/{memberId}/grant")
    public ResponseEntity<Void> grantPremium(
            @PathVariable Long memberId,
            @RequestBody AdminPremiumGrantRequest request
    ) {
        memberSubscriptionService.grantPremiumForAdmin(memberId, request.getGrantType());
        return ResponseEntity.ok().build();
    }

    /**
     * 관리자 프리미엄 권한 회수
     */
    @DeleteMapping("/{memberId}/revoke")
    public ResponseEntity<Void> revokePremium(
            @PathVariable Long memberId
    ) {
        memberSubscriptionService.revokePremiumForAdmin(memberId);
        return ResponseEntity.ok().build();
    }
}