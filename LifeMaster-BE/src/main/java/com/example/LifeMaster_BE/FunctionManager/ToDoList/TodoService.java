package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    // 모든 Todo 엔티티를 조회합니다.
    public List<TodoEntity> findAll() {
        return todoRepository.findAll();
    }

    // 새로운 Todo 엔티티를 생성합니다.
    public TodoEntity save(TodoEntity todo) {
        return todoRepository.save(todo);
    }

    // ID로 Todo 엔티티를 조회합니다.
    public Optional<TodoEntity> findById(Long id) {
        return todoRepository.findById(id); // Optional로 반환하여 존재 여부 확인 가능
    }

    // ID로 Todo 엔티티를 삭제합니다.
    public boolean deleteById(Long id) {
        if (todoRepository.existsById(id)) { // 존재 여부 확인
            todoRepository.deleteById(id);
            return true; // 삭제 성공
        }
        return false; // 삭제 실패
    }

    // ID로 Todo 엔티티를 업데이트합니다.
    public Optional<TodoEntity> update(Long id, TodoEntity todo) {
        if (todoRepository.existsById(id)) { // 존재 여부 확인
            todo.setId(id);
            return Optional.of(todoRepository.save(todo)); // 업데이트 성공
        }
        return Optional.empty(); // 업데이트 실패
    }
}

