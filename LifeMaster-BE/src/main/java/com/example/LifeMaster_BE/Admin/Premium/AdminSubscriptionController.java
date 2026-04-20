package com.example.LifeMaster_BE.Admin.Premium;

import com.example.LifeMaster_BE.UserManager.Member.Subscription.MemberSubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Admin - User", description = "관리자 프리미엄 계정 관리 API")
@RestController
@RequestMapping("/admin/premium")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final MemberSubscriptionService memberSubscriptionService;
    /**
     * 전체 프리미엄 유저 유효기간 조회
     */
    @GetMapping("/premium-members")
    public ResponseEntity<List<PremiumMemberAdminDto>> getAllPremiumMembers() {
        List<PremiumMemberAdminDto> result = memberSubscriptionService.getAllPremiumMembersForAdmin();
        return ResponseEntity.ok(result);
    }
}
