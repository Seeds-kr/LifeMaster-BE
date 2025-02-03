package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
    // 주어진 날짜에 해당하는 TodoEntity 목록을 반환
    List<TodoEntity> findByDate(String date);

    List<TodoEntity> findByDateAndTitle(String date, String title);
}
