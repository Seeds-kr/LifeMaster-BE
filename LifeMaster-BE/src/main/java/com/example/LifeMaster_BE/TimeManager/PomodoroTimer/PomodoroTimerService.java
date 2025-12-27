package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PomodoroTimerService {
    @Autowired
    private PomodoroTimerRepository repository;

    private final MemberRepository memberRepository;
    private final TodoRepository todoRepository;
    private final ScheduleCalendarService scheduleCalendarService;

    public PomodoroTimerService(MemberRepository memberRepository, TodoRepository todoRepository, ScheduleCalendarService scheduleCalendarService, PomodoroTimerRepository repository) {
        this.memberRepository = memberRepository;
        this.todoRepository = todoRepository;
        this.scheduleCalendarService = scheduleCalendarService;
        this.repository = repository;
    }

    /** 전체 포모도로 타이머 목록 조회 */
    public List<PomodoroTimerEntity> findAll() {
        return repository.findAll();
    }

    /** 특정 타이머 ID로 조회 */
    public Optional<PomodoroTimerEntity> findById(Long id) {
        return repository.findById(id);
    }

    /** 날짜별 타이머 조회 */
    public List<PomodoroTimerResponseDto> findByDate(String date) {
        return repository.findByDate(date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 새로운 포모도로 타이머 생성
     * - 멤버 및 ToDo 엔티티 유효성 검증
     * - 캘린더에 타이머 이벤트 추가
     */
    @Transactional
    public PomodoroTimerEntity create(PomodoroTimerDTO timerDto, Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        TodoEntity todo = null;
        if (timerDto.getTodoId() != null) {
            todo = todoRepository.findById(timerDto.getTodoId())
                    .orElseThrow(() -> new EntityNotFoundException("ToDo not found"));
        }

        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        scheduleCalendarService.addOrUpdateEvent(memberId, formattedDate, "pomodoroTimer");

        PomodoroTimerEntity pomodoroTimer = new PomodoroTimerEntity();
        pomodoroTimer.setMember(member);
        pomodoroTimer.setTodo(todo);
        pomodoroTimer.setDate(formattedDate);
        pomodoroTimer.setFocusTime(timerDto.getFocusTime());
        pomodoroTimer.setBreakTime(timerDto.getBreakTime());
        pomodoroTimer.setTaskName(timerDto.getTaskName());

        return repository.save(pomodoroTimer);
    }

    /** 해당 멤버의 모든 타이머 조회 */
    public List<PomodoroTimerEntity> getTimersByMember(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    /** 해당 멤버 + 특정 Todo에 연결된 타이머 조회 */
    public List<PomodoroTimerEntity> getTimersByMemberAndTodo(Long memberId, Long todoId) {
        return repository.findByMemberIdAndTodoId(memberId, todoId);
    }

    /** 특정 ToDo에 연결된 모든 타이머 삭제 */
    @Transactional
    public void deleteAllByTodoId(Long todoId) {
        repository.deleteAllByTodoId(todoId);
    }

    /** 개별 타이머 수정/저장 */
    @Transactional
    public PomodoroTimerEntity save(PomodoroTimerEntity pomodoroTimer) {
        return repository.save(pomodoroTimer);
    }

    /** 특정 ID의 타이머 삭제 */
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /** 특정 날짜의 타이머 전체 삭제 */
    @Transactional
    public void deleteAllByDate(String date) {
        repository.deleteAllByDate(date);
    }

    public List<PomodoroTimerResponseDto> getAllTimersAsDto() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PomodoroTimerResponseDto convertToDto(PomodoroTimerEntity timer) {
        Long memberId = (timer.getMember() != null) ? timer.getMember().getId() : null;
        return new PomodoroTimerResponseDto(
                timer.getId(),
                timer.getTaskName(),
                timer.getFocusTime(),
                timer.getBreakTime(),
                timer.getCurrentTimer(),
                timer.getDate(),
                memberId
        );
    }

}
