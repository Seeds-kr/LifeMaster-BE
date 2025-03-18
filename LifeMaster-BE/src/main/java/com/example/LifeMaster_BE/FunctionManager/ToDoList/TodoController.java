package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "To-Do List API", description = "To-Do List를 관리하는 API")
@RestController
@RequestMapping("/schedule/todo")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @Operation(summary = "모든 To-Do 항목 조회", description = "데이터베이스에 저장된 모든 To-Do 항목을 반환합니다.")
    @GetMapping
    public ResponseEntity<List<TodoEntity>> getAllTodos() {
        List<TodoEntity> todos = todoService.findAll();
        return ResponseEntity.ok(todos);
    }

    @Operation(
            summary = "새 To-Do 생성",
            description = "날짜와 제목을 기반으로 새로운 To-Do 항목을 추가합니다. 날짜 형식은 YYYYMMDD 입니다."
    )
    @PostMapping("/create")
    public ResponseEntity<TodoDTO> createTodo(@RequestBody TodoDTO todoDto,
                                              @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = user.getId();
        TodoDTO createdTodo = todoService.createTodo(todoDto, memberId);
        return new ResponseEntity<>(createdTodo, HttpStatus.CREATED);
    }

    @Operation(summary = "특정 To-Do 조회", description = "제목을 기반으로 특정 To-Do 항목을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<TodoEntity> getTodoById(@RequestParam("title") String title) {
        Optional<TodoEntity> todo = todoService.findByTitle(title);
        return todo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "To-Do 업데이트", description = "ID를 기반으로 기존의 To-Do 항목을 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<TodoEntity> updateTodo(@PathVariable("id") Long id,@RequestParam(value = "date", required = false) String date, @RequestParam(value = "title", required = false) String title) {
        Optional<TodoEntity> updatedTodo = todoService.updateDateTitle(id, date, title);
        return updatedTodo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "To-Do 삭제", description = "ID를 기반으로 To-Do 항목을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable("id") Long id) {
        if (todoService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "To-Do 완료 상태 토글", description = "ID를 기반으로 To-Do 항목의 완료 상태를 토글합니다.")
    @PatchMapping("/{id}/toggle-completed")
    public ResponseEntity<TodoEntity> toggleCompleted(@PathVariable("id") Long id) {
        Optional<TodoEntity> toggledTodo = todoService.toggleCompleted(id);
        return toggledTodo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
