package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
}
