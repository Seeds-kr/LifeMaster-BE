package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/schedule/todo") // API 구조를 위해
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ResponseEntity<List<TodoEntity>> getAllTodos() {
        List<TodoEntity> todos = todoService.findAll();
        return ResponseEntity.ok(todos); // 일관성을 위해 ResponseEntity 사용
    }

    @PostMapping
    public ResponseEntity<TodoEntity> createTodo(@RequestBody TodoEntity todo) {
        TodoEntity createdTodo = todoService.save(todo);
        return new ResponseEntity<>(createdTodo, HttpStatus.CREATED); // 생성 성공 상태 코드 반환
    }

    @GetMapping("/{id}") // 특정 Todo 항목 조회
    public ResponseEntity<TodoEntity> getTodoById(@PathVariable Long id) {
        Optional<TodoEntity> todo = todoService.findById(id);
        return todo != null ? (ResponseEntity<TodoEntity>) ResponseEntity.ok() : ResponseEntity.notFound().build(); // 항목이 없으면 404 반환
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoEntity> updateTodo(@PathVariable Long id, @RequestBody TodoEntity todo) {
        Optional<TodoEntity> updatedTodo = todoService.update(id, todo);
        return updatedTodo != null ? (ResponseEntity<TodoEntity>) ResponseEntity.ok() : ResponseEntity.notFound().build(); // 업데이트 실패 시 404 반환
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable("id") Long id) {
        if (todoService.deleteById(id)) { // 삭제 성공 여부에 따라 응답
            return ResponseEntity.noContent().build(); // 성공적으로 삭제된 경우 204 반환
        } else {
            return ResponseEntity.notFound().build(); // 항목이 없으면 404 반환
        }
    }
}

