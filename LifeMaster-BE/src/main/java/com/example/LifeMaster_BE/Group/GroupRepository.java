package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    // 초대 코드로 그룹 조회
    Optional<GroupEntity> findByInviteCode(String inviteCode);

    // 초대 코드 중복 확인
    boolean existsByInviteCode(String inviteCode);

    // 특정 유저 ID로 사용자가 속한 그룹의 목표 조회
    @Query("SELECT g.goals FROM GroupEntity g JOIN g.members m WHERE m.id = :memberId")
    List<GoalEntity> findGoalsByMemberId(@Param("memberId") Long memberId);

    // GET /group (전체 그룹 리스트)
    @Query("""
        select new com.example.LifeMaster_BE.Group.GroupResponseDto(
            g.id, g.icon, g.name, g.description, g.creator.id, count(distinct m)
        )
        from GroupEntity g
        left join g.members m
        group by g.id, g.icon, g.name, g.description, g.creator.id
    """)
    List<GroupResponseDto> findAllWithMemberCount();

    // GET /group/{id} (단건)
    @Query("""
        select new com.example.LifeMaster_BE.Group.GroupResponseDto(
            g.id, g.icon, g.name, g.description, g.creator.id, count(distinct m)
        )
        from GroupEntity g
        left join g.members m
        where g.id = :groupId
        group by g.id, g.icon, g.name, g.description, g.creator.id
    """)
    Optional<GroupResponseDto> findByIdWithMemberCount(@Param("groupId") Long groupId);

    // GET /group/user/me (내가 속한 그룹)
    @Query("""
        select new com.example.LifeMaster_BE.Group.GroupResponseDto(
            g.id, g.icon, g.name, g.description, g.creator.id, count(distinct m2)
        )
        from GroupEntity g
        join g.members me
        left join g.members m2
        where me.id = :memberId
        group by g.id, g.icon, g.name, g.description, g.creator.id
    """)
    List<GroupResponseDto> findMyGroupsWithMemberCount(@Param("memberId") Long memberId);
}