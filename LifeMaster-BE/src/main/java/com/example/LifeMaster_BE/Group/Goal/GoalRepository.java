package com.example.LifeMaster_BE.Group.Goal;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoalRepository extends JpaRepository<GoalEntity, Long> {

    void deleteByGroupId(Long groupId);

    /**
     * 특정 사용자가 참여 중인 그룹들에서
     * 특정 GoalType의 목표 조회
     *
     * 예:
     * userId = 3
     * goalType = SLEEP
     *
     * -> 해당 사용자가 속한 모든 그룹의 SLEEP 목표 반환
     */
    @Query("""
            SELECT DISTINCT g
            FROM GoalEntity g
            JOIN g.group grp
            JOIN grp.members member
            WHERE member.id = :userId
              AND g.goalType = :goalType
            """)
    List<GoalEntity> findGoalsByUserIdAndGoalType(
            @Param("userId") Long userId,
            @Param("goalType") GoalType goalType
    );
}