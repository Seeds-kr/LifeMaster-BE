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

        // 내 캘린더(date + memberId)로 찾거나 생성
        ScheduleCalendarEntity calendar = findOrCreateCalendar(memberId, dto.getDate());

        // 중복 체크도 memberId 기준으로
        // 1) TodoEntity에 member 필드가 있으니 이게 제일 깔끔함
        boolean exists = todoRepository.existsByMemberIdAndDateAndTitle(
                memberId, dto.getDate(), dto.getTitle()
        );
        if (exists) {
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

    public Optional<TodoEntity> updateDateTitle(Long memberId, Long id, String date, String title) {
        Optional<TodoEntity> optionalTodo = todoRepository.findById(id);
        if (optionalTodo.isEmpty()) return Optional.empty();

        TodoEntity todo = optionalTodo.get();

        // 내 Todo인지 검증
        // 1) TodoEntity에 member 필드가 있으면:
        // if (!todo.getMember().getId().equals(memberId)) return Optional.empty();
        //
        // 2) TodoEntity에 member가 없고 calendar->member로 연결되어 있다면:
        if (todo.getCalendar() == null
                || todo.getCalendar().getMember() == null
                || !todo.getCalendar().getMember().getId().equals(memberId)) {
            return Optional.empty(); // 또는 403 처리
        }

        // ✅ 날짜가 바뀌면 해당 날짜의 "내 캘린더"를 찾아/생성해서 연결
        if (date != null && !date.isBlank()) {
            ScheduleCalendarEntity calendar = findOrCreateCalendar(memberId, date);
            todo.setDate(date);
            todo.setCalendar(calendar);
        }

        if (title != null) {
            todo.setTitle(title);
        }

        return Optional.of(todoRepository.save(todo));
    }

    public Optional<TodoEntity> toggleCompleted(Long memberId, Long todoId) {
        Optional<TodoEntity> opt = todoRepository.findByIdAndCalendarMemberId(todoId, memberId);
        if (opt.isEmpty()) return Optional.empty();

        TodoEntity todo = opt.get();
        todo.setCompleted(!todo.isCompleted());
        return Optional.of(todoRepository.save(todo));
    }

    private ScheduleCalendarEntity findOrCreateCalendar(Long memberId, String date) {
        return calendarRepository.findByMemberIdAndDate(memberId, date)
                .orElseGet(() -> {
                    ScheduleCalendarEntity newCalendar = new ScheduleCalendarEntity();
                    newCalendar.setDate(date);
                    return scheduleCalendarService.createCalendarEntity(memberId, newCalendar);
                });
    }

    public TodoEntity addTodoToCalendar(Long memberId, String date, TodoEntity todo) {
        ScheduleCalendarEntity calendar = findOrCreateCalendar(memberId, date);
        todo.setCalendar(calendar);
        return todoRepository.save(todo);
    }

    public long countTodosByMemberIdAndDate(Long memberId, String date) {
        return todoRepository.countByMember_IdAndDate(memberId, date);
    }

    public List<TodoEntity> getTodosByMember(Long memberId) {
        return todoRepository.findByMemberId(memberId);
    }
}