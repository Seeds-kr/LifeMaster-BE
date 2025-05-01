package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarEntity;
import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarRepository;
import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.PomodoroTimerService;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
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

    private final PomodoroTimerService pomodoroTimerService;

    public TodoService(TodoRepository todoRepository,
                       ScheduleCalendarRepository calendarRepository,
                       ScheduleCalendarService scheduleCalendarService,
                       MemberRepository memberRepository,
                       PomodoroTimerService pomodoroTimerService) {
        this.todoRepository = todoRepository;
        this.calendarRepository = calendarRepository;
        this.scheduleCalendarService = scheduleCalendarService;
        this.memberRepository = memberRepository;
        this.pomodoroTimerService = pomodoroTimerService;
    }

    // 모든 Todo 엔티티를 조회합니다.
    public List<TodoEntity> findAll() {
        return todoRepository.findAll();
    }

    // 날짜와 제목만 입력받아 새로운 Todo 엔티티를 생성합니다.
    public TodoDTO createTodo(TodoDTO dto, Long memberId) {
        // 캘린더 찾거나 생성
        ScheduleCalendarEntity calendar = findOrCreateCalendar(dto.getDate());

        // 동일한 날짜와 제목의 Todo가 있는지 확인
        List<TodoEntity> existingTodos = todoRepository.findByDateAndTitle(dto.getDate(), dto.getTitle());
        if (!existingTodos.isEmpty()) {
            throw new IllegalArgumentException("The Todo with this title already exists for the given date.");
        }

        // 멤버 확인
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        // TodoEntity 생성 및 설정
        TodoEntity todo = new TodoEntity();
        todo.setDate(dto.getDate());
        todo.setTitle(dto.getTitle());
        todo.setCompleted(false);
        todo.setCalendar(calendar);
        todo.setMember(member);

        TodoEntity savedTodo = todoRepository.save(todo);

        // 저장된 엔티티를 DTO로 변환하여 반환
        return convertToDTO(savedTodo);
    }

    private TodoDTO convertToDTO(TodoEntity entity) {
        TodoDTO dto = new TodoDTO();
        dto.setDate(entity.getDate());
        dto.setTitle(entity.getTitle());
        return dto;
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
                // 1. 연결된 포모도로 타이머 먼저 삭제
                pomodoroTimerService.deleteAllByTodoId(todo.getId());

                // 2. 캘린더에서 ToDo도 제거
                ScheduleCalendarEntity calendar = todo.getCalendar();
                if (calendar != null) {
                    calendar.getTodos().remove(todo);
                }

                // 3. ToDo 삭제
                todoRepository.deleteById(todo.getId());
            });
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
            if(title != null)
                todo.setTitle(title);
            if(date != null)
                todo.setDate(date);

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

    // 유저별 todo 리스트 조회
    public List<TodoEntity> getTodosByMember(Long memberId) {
        return todoRepository.findByMemberId(memberId);
    }
}

