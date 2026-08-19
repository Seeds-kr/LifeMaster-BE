package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PomodoroTimerRepository extends JpaRepository<PomodoroTimerEntity, Long> {

    // 날짜별 전체 조회
    List<PomodoroTimerEntity> findByDate(String date);

    // 회원별 전체 조회
    List<PomodoroTimerEntity> findByMemberId(Long memberId);

    // 회원 + Todo별 조회
    List<PomodoroTimerEntity> findByMemberIdAndTodoId(Long memberId, Long todoId);

    // 회원 + 날짜 조회
    List<PomodoroTimerEntity> findByMember_IdAndDate(Long memberId, String date);

    // 회원 + 날짜 범위 조회
    List<PomodoroTimerEntity> findByMember_IdAndDateBetween(
            Long memberId,
            String startDate,
            String endDate
    );

    // ID + 회원 기준 조회
    Optional<PomodoroTimerEntity> findByIdAndMember_Id(Long id, Long memberId);
}