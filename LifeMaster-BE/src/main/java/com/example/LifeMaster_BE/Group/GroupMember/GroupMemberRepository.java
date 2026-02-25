package com.example.LifeMaster_BE.Group.GroupMember;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {

    // ===== 존재/가입 여부 =====
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    // ===== 단건 조회 =====
    Optional<GroupMemberEntity> findByGroupIdAndUserId(Long groupId, Long userId);

    // ===== 그룹 기준 멤버 목록 =====
    List<GroupMemberEntity> findAllByGroupId(Long groupId);

    // ===== 유저 기준 소속 그룹 목록 =====
    List<GroupMemberEntity> findAllByUserId(Long userId);

    // ===== 삭제 =====
    long deleteByGroupIdAndUserId(Long groupId, Long userId);

    @Modifying
    @Query("delete from GroupMemberEntity gm where gm.groupId = :groupId")
    void deleteAllByGroupId(@Param("groupId") Long groupId);

    // 현재 OWNER를 PESSIMISTIC_WRITE로 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select gm from GroupMemberEntity gm where gm.groupId = :groupId and gm.role = 'OWNER'")
    Optional<GroupMemberEntity> findOwnerForUpdate(@Param("groupId") Long groupId);
}
