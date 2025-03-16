package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarEntity;
import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarRepository;
import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final ScheduleCalendarRepository calendarRepository;

    private final ScheduleCalendarService scheduleCalendarService;
    private final MemberRepository memberRepository;

    public TodoService(TodoRepository todoRepository, ScheduleCalendarRepository calendarRepository, ScheduleCalendarService scheduleCalendarService, MemberRepository memberRepository) {
        this.todoRepository = todoRepository;
        this.calendarRepository = calendarRepository;
        this.scheduleCalendarService = scheduleCalendarService;
        this.memberRepository = memberRepository;
    }

    // 모든 Todo 엔티티를 조회합니다.
    public List<TodoEntity> findAll() {
        return todoRepository.findAll();
    }

    // 날짜와 제목만 입력받아 새로운 Todo 엔티티를 생성합니다.
    public TodoEntity createTodo(String date, String title,Long memberId) {
        // 날짜에 해당하는 캘린더 조회 또는 생성
        ScheduleCalendarEntity calendar = findOrCreateCalendar(date);

        // 동일한 날짜에 동일한 제목을 가진 Todo가 있는지 확인
        List<TodoEntity> existingTodos = todoRepository.findByDateAndTitle(date, title);
        if (!existingTodos.isEmpty()) {
            // 동일한 제목의 Todo가 이미 존재하는 경우
            throw new IllegalArgumentException("The Todo with this title already exists for the given date.");
        }

        //멤베 id 존재하는지 검증
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        // TodoEntity 생성 및 설정
        TodoEntity todo = new TodoEntity();
        todo.setDate(date);
        todo.setTitle(title);
        todo.setId(memberId);
        todo.setCompleted(false); // 기본값 설정
        todo.setCalendar(calendar); // 캘린더 연결

        return todoRepository.save(todo);
    }

    // ID로 Todo 엔티티를 조회합니다.
    public Optional<TodoEntity> findById(Long id) {
        return todoRepository.findById(id);
    }
    public Optional<TodoEntity> findByTitle(String title) {
        return todoRepository.findByTitle(title);
    }

    // ID로 Todo 엔티티를 삭제합니다.
    public boolean deleteById(Long id) {
        if (todoRepository.existsById(id)) {
            Optional<TodoEntity> todoOptional = todoRepository.findById(id);
            todoOptional.ifPresent(todo -> {
                // 삭제 전에 해당 캘린더의 To-Do 삭제
                ScheduleCalendarEntity calendar = todo.getCalendar();
                if (calendar != null) {
                    // 캘린더에서 Todo 삭제
                    calendar.getTodos().remove(todo);
                }
            });
            todoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 날짜와 제목을 수정합니다.
    public Optional<TodoEntity> updateDateTitle(Long id, String date, String title) {
        Optional<TodoEntity> optionalTodo = todoRepository.findById(id);

        if (optionalTodo.isPresent()) {
            TodoEntity todo = optionalTodo.get();

            // 날짜에 해당하는 캘린더 조회 또는 생성
            ScheduleCalendarEntity calendar = findOrCreateCalendar(date);

            // 날짜와 제목 수정
            todo.setDate(date);
            todo.setTitle(title);
            todo.setCalendar(calendar); // 캘린더 설정

            // 저장 및 반환
            return Optional.of(todoRepository.save(todo));
        }

        return Optional.empty(); // ID가 존재하지 않는 경우
    }

    // ID로 Todo 엔티티의 completed 상태를 반전시킵니다.
    public Optional<TodoEntity> toggleCompleted(Long id) {
        Optional<TodoEntity> todoOptional = todoRepository.findById(id);
        if (todoOptional.isPresent()) {
            TodoEntity todo = todoOptional.get();
            todo.setCompleted(!todo.isCompleted()); // completed 상태 반전
            return Optional.of(todoRepository.save(todo));
        }
        return Optional.empty();
    }

    // 특정 날짜의 캘린더를 조회하거나 생성
    private ScheduleCalendarEntity findOrCreateCalendar(String date) {
        return calendarRepository.findByDate(date)
                .orElseGet(() -> {
                    ScheduleCalendarEntity newCalendar = new ScheduleCalendarEntity();
                    newCalendar.setDate(date);
                    return scheduleCalendarService.createCalendarEntity(newCalendar);
                });
    }

    // 특정 날짜의 To-Do 추가
    public TodoEntity addTodoToCalendar(String date, TodoEntity todo) {
        // 날짜에 해당하는 캘린더 조회 또는 생성
        ScheduleCalendarEntity calendar = findOrCreateCalendar(date);

        // To-Do를 캘린더에 추가
        todo.setCalendar(calendar);
        return todoRepository.save(todo);
    }
}

