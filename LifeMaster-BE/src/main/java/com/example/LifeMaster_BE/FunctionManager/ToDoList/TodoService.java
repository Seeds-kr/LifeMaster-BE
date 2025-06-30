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

    public List<TodoEntity> findAll() {
        return todoRepository.findAll();
    }

    public TodoResponse createTodo(TodoCreateRequest dto, Long memberId) {
        ScheduleCalendarEntity calendar = findOrCreateCalendar(dto.getDate());

        List<TodoEntity> existingTodos = todoRepository.findByDateAndTitle(dto.getDate(), dto.getTitle());
        if (!existingTodos.isEmpty()) {
            throw new IllegalArgumentException("The Todo with this title already exists for the given date.");
        }

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        TodoEntity todo = new TodoEntity();
        todo.setDate(dto.getDate());
        todo.setTitle(dto.getTitle());
        todo.setCompleted(false);
        todo.setCalendar(calendar);
        todo.setMember(member);

        TodoEntity savedTodo = todoRepository.save(todo);

        return convertToResponse(savedTodo);
    }

    private TodoResponse convertToResponse(TodoEntity entity) {
        TodoResponse dto = new TodoResponse();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setTitle(entity.getTitle());
        dto.setCompleted(entity.isCompleted());
        return dto;
    }

    public Optional<TodoEntity> findById(Long id) {
        return todoRepository.findById(id);
    }

    public Optional<TodoEntity> findByTitle(String title) {
        return todoRepository.findByTitle(title);
    }

    public boolean deleteById(Long id) {
        if (todoRepository.existsById(id)) {
            Optional<TodoEntity> todoOptional = todoRepository.findById(id);
            todoOptional.ifPresent(todo -> {
                pomodoroTimerService.deleteAllByTodoId(todo.getId());

                ScheduleCalendarEntity calendar = todo.getCalendar();
                if (calendar != null) {
                    calendar.getTodos().remove(todo);
                }

                todoRepository.deleteById(todo.getId());
            });
            return true;
        }
        return false;
    }

    public Optional<TodoEntity> updateDateTitle(Long id, String date, String title) {
        Optional<TodoEntity> optionalTodo = todoRepository.findById(id);

        if (optionalTodo.isPresent()) {
            TodoEntity todo = optionalTodo.get();

            ScheduleCalendarEntity calendar = findOrCreateCalendar(date);

            if (title != null)
                todo.setTitle(title);
            if (date != null)
                todo.setDate(date);

            todo.setCalendar(calendar);

            return Optional.of(todoRepository.save(todo));
        }

        return Optional.empty();
    }

    public Optional<TodoEntity> toggleCompleted(Long id) {
        Optional<TodoEntity> todoOptional = todoRepository.findById(id);
        if (todoOptional.isPresent()) {
            TodoEntity todo = todoOptional.get();
            todo.setCompleted(!todo.isCompleted());
            return Optional.of(todoRepository.save(todo));
        }
        return Optional.empty();
    }

    private ScheduleCalendarEntity findOrCreateCalendar(String date) {
        return calendarRepository.findByDate(date)
                .orElseGet(() -> {
                    ScheduleCalendarEntity newCalendar = new ScheduleCalendarEntity();
                    newCalendar.setDate(date);
                    return scheduleCalendarService.createCalendarEntity(newCalendar);
                });
    }

    public TodoEntity addTodoToCalendar(String date, TodoEntity todo) {
        ScheduleCalendarEntity calendar = findOrCreateCalendar(date);
        todo.setCalendar(calendar);
        return todoRepository.save(todo);
    }

    public List<TodoEntity> getTodosByMember(Long memberId) {
        return todoRepository.findByMemberId(memberId);
    }
}