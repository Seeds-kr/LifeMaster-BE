package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
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
    private final ScheduleCalendarService scheduleCalendarService;
    private final Login login;

    public TodoController(
            TodoService todoService,
            ScheduleCalendarService scheduleCalendarService,
            Login login
    ) {
        this.todoService = todoService;
        this.scheduleCalendarService = scheduleCalendarService;
        this.login = login;
    }

    @Operation(
            summary = "모든 To-Do 항목 조회",
            description = "데이터베이스에 저장된 모든 To-Do 항목을 반환합니다."
    )
    @GetMapping
    public ResponseEntity<?> getAllTodos(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        List<TodoEntity> todos = todoService.findAll();
        return ResponseEntity.ok(todos);
    }

    @Operation(
            summary = "새 To-Do 생성",
            description = "날짜와 제목을 기반으로 새로운 To-Do 항목을 추가합니다. 날짜 형식은 YYYYMMDD 입니다."
    )
    @PostMapping("/create")
    public ResponseEntity<?> createTodo(
            @RequestBody TodoCreateRequest todoCreateRequest,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        TodoResponse createdTodo =
                todoService.createTodo(todoCreateRequest, memberId);

        scheduleCalendarService.addOrUpdateEvent(
                memberId,
                createdTodo.getDate(),
                "Todo"
        );

        return new ResponseEntity<>(
                createdTodo,
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "특정 To-Do 조회",
            description = "제목을 기반으로 특정 To-Do 항목을 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getTodoByTitle(
            @RequestParam("title") String title,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Optional<TodoEntity> todo =
                todoService.findByTitle(title);

        return todo.map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @Operation(
            summary = "To-Do 업데이트",
            description = "ID를 기반으로 기존의 To-Do 항목을 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(
            @PathVariable("id") Long id,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "title", required = false) String title,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        Optional<TodoEntity> updatedTodoOpt =
                todoService.updateDateTitle(
                        memberId,
                        id,
                        date,
                        title
                );

        if (updatedTodoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TodoEntity updatedTodo = updatedTodoOpt.get();

        String targetDate = updatedTodo.getDate();

        scheduleCalendarService.addOrUpdateEvent(
                memberId,
                targetDate,
                "Todo"
        );

        return ResponseEntity.ok(updatedTodo);
    }

    @Operation(
            summary = "To-Do 삭제",
            description = "ID를 기반으로 로그인한 사용자의 To-Do 항목을 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        Optional<TodoEntity> todoOpt =
                todoService.findByIdAndMemberId(
                        id,
                        memberId
                );

        if (todoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TodoEntity todo = todoOpt.get();
        String date = todo.getDate();

        boolean deleted =
                todoService.deleteById(
                        memberId,
                        id
                );

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        if (todoService.countTodosByMemberIdAndDate(
                memberId,
                date
        ) == 0) {
            scheduleCalendarService.deleteSpecificEvent(
                    memberId,
                    date,
                    "Todo"
            );
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "To-Do 완료 상태 토글",
            description = "ID를 기반으로 To-Do 항목의 완료 상태를 토글합니다."
    )
    @PatchMapping("/{id}/toggle-completed")
    public ResponseEntity<?> toggleCompleted(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        Optional<TodoEntity> toggledTodoOpt =
                todoService.toggleCompleted(
                        memberId,
                        id
                );

        if (toggledTodoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TodoEntity toggledTodo =
                toggledTodoOpt.get();

        String targetDate =
                toggledTodo.getDate();

        scheduleCalendarService.addOrUpdateEvent(
                memberId,
                targetDate,
                "Todo"
        );

        return ResponseEntity.ok(toggledTodo);
    }

    @Operation(
            summary = "현재 유저의 To-Do 조회",
            description = "로그인한 유저의 To-Do 항목을 조회합니다."
    )
    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getTodosByMember(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        return ResponseEntity.ok(
                todoService.getTodosByMember(
                        user.getId()
                )
        );
    }

    @Operation(
            summary = "현재 유저의 날짜별 To-Do 조회",
            description = "로그인한 유저의 특정 날짜 To-Do 항목을 조회합니다. 날짜 형식은 YYYYMMDD 입니다."
    )
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getTodosByDate(
            @PathVariable("date") String date,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        return ResponseEntity.ok(
                todoService.getTodosByMemberAndDate(
                        memberId,
                        date
                )
        );
    }
}