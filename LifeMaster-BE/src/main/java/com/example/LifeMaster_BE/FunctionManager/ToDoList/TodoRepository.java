package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
    // 주어진 날짜에 해당하는 TodoEntity 목록을 반환
    List<TodoEntity> findByDate(String date);
    Optional<TodoEntity> findByTitle(String title);

    List<TodoEntity> findByDateAndTitle(String date, String title);

    List<TodoEntity> findByMemberId(Long memberId);

    long countByMember_IdAndDate(Long memberId, String date);

    boolean existsByMemberId(Long memberId);

    List<TodoEntity> findByMemberIdAndDate(Long memberId, String date);

    boolean existsByMemberIdAndDateAndTitle(Long memberId, String date, String title);

    Optional<TodoEntity> findByIdAndCalendarMemberId(Long id, Long memberId);

    long countByMemberId(Long memberId);
}
