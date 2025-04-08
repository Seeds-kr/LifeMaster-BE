package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.List;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    // 특정 유저 ID로 사용자가 속한 그룹의 목표 조회
    @Query("SELECT g.goals FROM GroupEntity g JOIN g.members m WHERE m.id = :memberId")
    List<GoalEntity> findGoalsByMemberId(@Param("memberId") Long memberId);
}
