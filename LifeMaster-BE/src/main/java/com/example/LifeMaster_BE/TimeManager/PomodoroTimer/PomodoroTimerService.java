package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PomodoroTimerService {
    @Autowired
    private PomodoroTimerRepository repository;

    private final MemberRepository memberRepository;

    private final TodoRepository todoRepository ;

    private final ScheduleCalendarService scheduleCalendarService;

    public PomodoroTimerService(MemberRepository memberRepository, TodoRepository todoRepository, ScheduleCalendarService scheduleCalendarService) {
        this.memberRepository = memberRepository;
        this.todoRepository = todoRepository;
        this.scheduleCalendarService = scheduleCalendarService;
    }

    public List<PomodoroTimerEntity> findAll() {
        return repository.findAll();
    }

    public Optional<PomodoroTimerEntity> findById(Long id) {
        return repository.findById(id);
    }

    public List<PomodoroTimerEntity> findByDate(String date) {
        return repository.findByDate(date);
    }

    public PomodoroTimerEntity create(PomodoroTimerDTO timerDto, Long memberId) {
        // 멤버 확인
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        // ToDoEntity 확인 및 연관 관계 설정
        TodoEntity todo = null;
        if (timerDto.getTodoId() != null) {
            todo = todoRepository.findById(timerDto.getTodoId())
                    .orElseThrow(() -> new EntityNotFoundException("ToDo not found"));
        }
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        scheduleCalendarService.addOrUpdateEvent(formattedDate, "pomodoroTimer");
        // PomodoroTimerEntity 객체 생성 및 값 설정
        PomodoroTimerEntity pomodoroTimer = new PomodoroTimerEntity();
        pomodoroTimer.setMember(member);
        pomodoroTimer.setTodo(todo); // ToDoEntity 연결
        pomodoroTimer.setDate(formattedDate);
        pomodoroTimer.setFocusTime(timerDto.getFocusTime());
        pomodoroTimer.setBreakTime(timerDto.getBreakTime());
        pomodoroTimer.setTaskName(timerDto.getTaskName());

        return repository.save(pomodoroTimer);
    }

    // 유저별 전체 타이머 조회
    public List<PomodoroTimerEntity> getTimersByMember(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    // 유저 + 특정 TODO 기준 타이머 조회
    public List<PomodoroTimerEntity> getTimersByMemberAndTodo(Long memberId, Long todoId) {
        return repository.findByMemberIdAndTodoId(memberId, todoId);
    }

    public PomodoroTimerEntity save(PomodoroTimerEntity pomodoroTimer) {
        return repository.save(pomodoroTimer);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public void deleteAllByDate(String date) {
        repository.deleteAllByDate(date);  // 날짜 기준 모든 타이머 삭제
    }
}
