package com.example.LifeMaster_BE.Admin.Member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

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
}
