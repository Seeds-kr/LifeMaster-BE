package com.example.LifeMaster_BE.Group.GroupMember;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;

    // ===== 내부 공통: 멤버 조회(없으면 예외) =====
    @Transactional(readOnly = true)
    public GroupMemberEntity getMemberOrThrow(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new GroupMemberException("Group member not found. groupId=" + groupId + ", userId=" + userId));
    }

    // ===== 권한 체크 유틸 =====
    @Transactional(readOnly = true)
    public GroupMemberRole getRole(Long groupId, Long userId) {
        return getMemberOrThrow(groupId, userId).getRole();
    }

    @Transactional(readOnly = true)
    public boolean isMember(Long groupId, Long userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    @Transactional(readOnly = true)
    public void requireAtLeastAdmin(Long groupId, Long requestUserId) {
        GroupMemberRole role = getRole(groupId, requestUserId);
        if (role != GroupMemberRole.OWNER && role != GroupMemberRole.ADMIN) {
            throw new GroupMemberException("Permission denied. ADMIN+ required.");
        }
    }

    @Transactional(readOnly = true)
    public void requireOwner(Long groupId, Long requestUserId) {
        GroupMemberRole role = getRole(groupId, requestUserId);
        if (role != GroupMemberRole.OWNER) {
            throw new GroupMemberException("Permission denied. OWNER required.");
        }
    }

    // ===== 멤버 추가 =====
    public GroupMemberEntity addMember(Long groupId, Long requestUserId, Long targetUserId, GroupMemberRole role) {
        // 권한: ADMIN 이상만 초대 가능(원하면 OWNER만 가능으로 바꿔도 됨)
        requireAtLeastAdmin(groupId, requestUserId);

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)) {
            throw new GroupMemberException("Already joined.");
        }

        // 기본값 MEMBER 추천
        GroupMemberRole finalRole = (role == null) ? GroupMemberRole.MEMBER : role;

        // 안전장치: OWNER는 서비스에서 막고, 그룹 생성 시에만 부여하는 걸 추천
        if (finalRole == GroupMemberRole.OWNER) {
            throw new GroupMemberException("Cannot assign OWNER via addMember.");
        }

        GroupMemberEntity entity = GroupMemberEntity.builder()
                .groupId(groupId)
                .userId(targetUserId)
                .role(finalRole)
                .build();

        return groupMemberRepository.save(entity);
    }

    // ===== 멤버 제거(강퇴/탈퇴 공용) =====
    public void removeMember(Long groupId, Long requestUserId, Long targetUserId) {
        // 본인 탈퇴는 허용 / 타인 제거는 ADMIN+ 필요
        boolean self = requestUserId.equals(targetUserId);
        if (!self) {
            requireAtLeastAdmin(groupId, requestUserId);
        }

        GroupMemberEntity target = getMemberOrThrow(groupId, targetUserId);

        // OWNER 보호: OWNER는 소유권 이전 로직 없으면 제거 금지
        if (target.getRole() == GroupMemberRole.OWNER) {
            throw new GroupMemberException("OWNER cannot be removed. Transfer ownership first.");
        }

        long deleted = groupMemberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
        if (deleted == 0) {
            throw new GroupMemberException("Failed to remove member (not found).");
        }
    }

    // ===== 등급 변경 =====
    public GroupMemberEntity changeRole(Long groupId, Long requestUserId, Long targetUserId, GroupMemberRole newRole) {
        if (newRole == null) throw new GroupMemberException("newRole is required.");

        GroupMemberEntity requester = getMemberOrThrow(groupId, requestUserId);
        GroupMemberEntity target = getMemberOrThrow(groupId, targetUserId);

        // OWNER 보호
        if (target.getRole() == GroupMemberRole.OWNER) {
            throw new GroupMemberException("Cannot change OWNER role.");
        }

        // 권한 규칙:
        // - OWNER: 누구든 변경 가능(OWNER 제외)
        // - ADMIN: MEMBER만 변경 가능(ADMIN끼리/OWNER는 불가)
        if (requester.getRole() == GroupMemberRole.ADMIN) {
            if (target.getRole() != GroupMemberRole.MEMBER) {
                throw new GroupMemberException("ADMIN can only change MEMBER roles.");
            }
            if (newRole == GroupMemberRole.OWNER) {
                throw new GroupMemberException("Cannot assign OWNER.");
            }
        } else if (requester.getRole() != GroupMemberRole.OWNER) {
            throw new GroupMemberException("Permission denied. OWNER/ADMIN required.");
        }

        // newRole 제한(OWNER는 별도 transferOwnership에서만)
        if (newRole == GroupMemberRole.OWNER) {
            throw new GroupMemberException("Use transferOwnership to assign OWNER.");
        }

        target.changeRole(newRole);
        return groupMemberRepository.save(target);
    }

    // ===== 소유권 이전 (선택 기능) =====
    public void transferOwnership(Long groupId, Long requestUserId, Long newOwnerUserId) {

        // 1) 현재 OWNER row를 락으로 잡아 동시성 차단
        GroupMemberEntity currentOwner = groupMemberRepository.findOwnerForUpdate(groupId)
                .orElseThrow(() -> new GroupMemberException("OWNER not found for groupId=" + groupId));

        // 2) 요청자가 진짜 OWNER인지 확인
        if (!currentOwner.getUserId().equals(requestUserId)) {
            throw new GroupMemberException("Permission denied. OWNER required.");
        }

        // 3) 새 OWNER는 같은 그룹 멤버여야 함
        GroupMemberEntity newOwner = getMemberOrThrow(groupId, newOwnerUserId);

        if (newOwner.getRole() == GroupMemberRole.OWNER) return;

        // 4) OWNER 교체 (같은 트랜잭션에서 순서대로)
        currentOwner.changeRole(GroupMemberRole.ADMIN);
        newOwner.changeRole(GroupMemberRole.OWNER);

        groupMemberRepository.save(currentOwner);
        groupMemberRepository.save(newOwner);
    }

    // ===== 조회 =====
    @Transactional(readOnly = true)
    public List<GroupMemberEntity> getMembersByGroup(Long groupId, Long requestUserId) {
        // 멤버만 조회 가능하게
        getMemberOrThrow(groupId, requestUserId);
        return groupMemberRepository.findAllByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberEntity> getGroupsByUser(Long userId) {
        return groupMemberRepository.findAllByUserId(userId);
    }

    // 그룹 생성 시: 생성자 OWNER 등록 (이미 있으면 스킵)
    public GroupMemberEntity ensureOwner(Long groupId, Long creatorUserId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, creatorUserId)
                .orElseGet(() -> groupMemberRepository.save(
                        GroupMemberEntity.builder()
                                .groupId(groupId)
                                .userId(creatorUserId)
                                .role(GroupMemberRole.OWNER)
                                .build()
                ));
    }

    // 초대 코드 가입용: 권한 체크 없이 MEMBER로 등록
    public GroupMemberEntity joinAsMember(Long groupId, Long userId) {
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GroupMemberException("Already joined.");
        }

        GroupMemberEntity entity = GroupMemberEntity.builder()
                .groupId(groupId)
                .userId(userId)
                .role(GroupMemberRole.MEMBER)
                .build();

        return groupMemberRepository.save(entity);
    }

    // 그룹 삭제/정리용
    public void deleteAllByGroupId(Long groupId) {
        groupMemberRepository.deleteAllByGroupId(groupId);
    }
}
