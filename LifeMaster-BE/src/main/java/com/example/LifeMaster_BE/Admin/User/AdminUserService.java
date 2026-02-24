package com.example.LifeMaster_BE.Admin.User;

import com.example.LifeMaster_BE.Admin.User.Dto.AdminUserDetailDto;
import com.example.LifeMaster_BE.Admin.User.Dto.AdminUserListDto;
import com.example.LifeMaster_BE.Admin.User.Dto.RoleUpdateRequest;
import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final MemberRepository memberRepository;

    public Page<AdminUserListDto> getUsers(String keyword, MemberStatus status, Pageable pageable) {
        Page<MemberEntity> members = memberRepository.searchMembers(keyword, status, pageable);
        return members.map(AdminUserListDto::from);
    }

    public AdminUserDetailDto getUserDetail(Long userId) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + userId));
        return AdminUserDetailDto.from(member);
    }

    @Transactional
    public void updateUserRole(Long userId, RoleUpdateRequest request) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + userId));
        member.setLoginRole(request.getLoginRole());
    }

    @Transactional
    public void deleteUser(Long userId) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + userId));
        member.setMemberStatus(MemberStatus.DELETED);
    }

    @Transactional
    public void updateUserStatus(Long userId, MemberStatus status) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + userId));
        member.setMemberStatus(status);
    }

    @Transactional
    public void addWarning(Long userId) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + userId));
        member.setWarningCount(member.getWarningCount() + 1);
        member.setMemberStatus(MemberStatus.WARNED);
    }
}
