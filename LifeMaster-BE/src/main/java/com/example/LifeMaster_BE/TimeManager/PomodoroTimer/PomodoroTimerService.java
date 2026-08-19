package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoRepository;
import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto.*;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PomodoroTimerService {

    private final PomodoroTimerRepository repository;
    private final MemberRepository memberRepository;
    private final TodoRepository todoRepository;
    private final ScheduleCalendarService scheduleCalendarService;
    private final PomodoroDailyFocusRepository dailyFocusRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int BASELINE_DAYS = 30;
    private static final int RECENT_DAYS = 7;

    public PomodoroTimerService(
            MemberRepository memberRepository,
            TodoRepository todoRepository,
            ScheduleCalendarService scheduleCalendarService,
            PomodoroTimerRepository repository,
            PomodoroDailyFocusRepository dailyFocusRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.memberRepository = memberRepository;
        this.todoRepository = todoRepository;
        this.scheduleCalendarService = scheduleCalendarService;
        this.repository = repository;
        this.dailyFocusRepository = dailyFocusRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<PomodoroTimerEntity> findAll() {
        return repository.findAll();
    }

    public Optional<PomodoroTimerEntity> findById(Long id) {
        return repository.findById(id);
    }

    // 로그인 사용자 소유 타이머 조회
    public Optional<PomodoroTimerEntity> findByIdAndMember(Long id, Long memberId) {
        return repository.findByIdAndMember_Id(id, memberId);
    }

    public List<PomodoroTimerResponseDto> findByDate(String date) {
        return repository.findByDate(date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PomodoroTimerEntity create(PomodoroTimerDTO timerDto, Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        TodoEntity todo = null;
        if (timerDto.getTodoId() != null) {
            todo = todoRepository.findById(timerDto.getTodoId())
                    .orElseThrow(() -> new EntityNotFoundException("ToDo not found"));
        }

        if (timerDto.getTaskName() == null || timerDto.getTaskName().isBlank()) {
            throw new IllegalArgumentException("taskName is required.");
        }
        if (timerDto.getFocusTime() <= 0) {
            throw new IllegalArgumentException("focusTime must be greater than 0.");
        }
        if (timerDto.getBreakTime() < 0) {
            throw new IllegalArgumentException("breakTime must be 0 or greater.");
        }
        if (timerDto.getDate() == null || timerDto.getDate().isBlank()) {
            throw new IllegalArgumentException("date is required. format: yyyy-MM-dd");
        }

        LocalDate parsedDate = parseApiDate(timerDto.getDate());
        String formattedDate = toTimerDateKey(parsedDate);

        scheduleCalendarService.addOrUpdateEvent(memberId, formattedDate, "pomodoroTimer");

        PomodoroTimerEntity timer = new PomodoroTimerEntity();
        timer.setMember(member);
        timer.setTodo(todo);
        timer.setDate(formattedDate);
        timer.setFocusTime(timerDto.getFocusTime());
        timer.setBreakTime(timerDto.getBreakTime());
        timer.setTaskName(timerDto.getTaskName());
        timer.setCurrentTimer(0);
        timer.setCompletedCount(0);

        return repository.save(timer);
    }

    public List<PomodoroTimerEntity> getTimersByMember(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    public List<PomodoroTimerEntity> getTimersByMemberAndTodo(Long memberId, Long todoId) {
        return repository.findByMemberIdAndTodoId(memberId, todoId);
    }

    // 로그인 사용자의 특정 Todo 포모도로 전체 삭제
    @Transactional
    public void deleteAllByTodoId(Long memberId, Long todoId) {
        List<PomodoroTimerEntity> timers = repository.findByMemberIdAndTodoId(memberId, todoId);

        if (timers.isEmpty()) return;

        Set<String> dates = timers.stream()
                .map(PomodoroTimerEntity::getDate)
                .collect(Collectors.toSet());

        repository.deleteAll(timers);
        repository.flush();

        for (String date : dates) {
            publishPomodoroEvent(memberId, date);
        }
    }

    @Transactional
    public PomodoroTimerEntity save(PomodoroTimerEntity pomodoroTimer) {
        PomodoroTimerEntity saved = repository.save(pomodoroTimer);

        if (saved.getMember() != null && saved.getDate() != null) {
            publishPomodoroEvent(saved.getMember().getId(), saved.getDate());
        }

        return saved;
    }

    // 로그인 사용자의 특정 포모도로 삭제
    @Transactional
    public void deleteById(Long memberId, Long id) {
        PomodoroTimerEntity timer = repository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Pomodoro timer not found"));

        String date = timer.getDate();

        repository.delete(timer);
        repository.flush();

        publishPomodoroEvent(memberId, date);
    }

    // 로그인 사용자의 특정 날짜 포모도로 전체 삭제
    @Transactional
    public void deleteAllByDate(Long memberId, String date) {
        List<PomodoroTimerEntity> timers = repository.findByMember_IdAndDate(memberId, date);

        if (timers.isEmpty()) return;

        repository.deleteAll(timers);
        repository.flush();

        publishPomodoroEvent(memberId, date);
    }

    public List<PomodoroTimerResponseDto> getAllTimersAsDto() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PomodoroStatsResponseDto getPomodoroStats(Long memberId, String date) {
        LocalDate targetDate = parseApiDate(date);
        String apiDate = targetDate.format(API_DATE_FORMATTER);

        DailyPomodoroSummary todaySummary = calculateDailyPomodoroSummary(memberId, targetDate);

        int todayTotalFocusMinutes = todaySummary.totalFocusMinutes;
        int completedCount = todaySummary.completedCount;
        int averageFocusMinutes = todaySummary.averageFocusMinutes;

        LocalDate baselineStartDate = targetDate.minusDays(BASELINE_DAYS);
        LocalDate baselineEndDate = targetDate.minusDays(1);

        Map<String, DailyPomodoroSummary> baselineSummaryMap =
                calculateDailyPomodoroSummaryMap(memberId, baselineStartDate, baselineEndDate);

        int baselineTotalFocusMinutesSum = 0;
        int baselineCompletedCountSum = 0;
        int baselineAverageFocusMinutesSum = 0;
        int activeBaselineDays = 0;

        for (int i = 0; i < BASELINE_DAYS; i++) {
            LocalDate currentDate = baselineStartDate.plusDays(i);
            String currentApiDate = currentDate.format(API_DATE_FORMATTER);

            DailyPomodoroSummary summary =
                    baselineSummaryMap.getOrDefault(currentApiDate, DailyPomodoroSummary.empty());

            if (summary.completedCount > 0) {
                activeBaselineDays++;
                baselineTotalFocusMinutesSum += summary.totalFocusMinutes;
                baselineCompletedCountSum += summary.completedCount;
                baselineAverageFocusMinutesSum += summary.averageFocusMinutes;
            }
        }

        int baselineTotalFocusAverage = activeBaselineDays == 0
                ? 0 : Math.round((float) baselineTotalFocusMinutesSum / activeBaselineDays);

        int focusMinutesDiff = todayTotalFocusMinutes - baselineTotalFocusAverage;

        int baselineCompletedCountAverage = activeBaselineDays == 0
                ? 0 : Math.round((float) baselineCompletedCountSum / activeBaselineDays);

        int completedCountDiff = completedCount - baselineCompletedCountAverage;

        int baselineAverageFocusMinutes = activeBaselineDays == 0
                ? 0 : Math.round((float) baselineAverageFocusMinutesSum / activeBaselineDays);

        int averageFocusMinutesDiff = averageFocusMinutes - baselineAverageFocusMinutes;

        LocalDate weekStartDate = targetDate.minusDays(RECENT_DAYS - 1);
        LocalDate weekEndDate = targetDate;

        Map<String, DailyPomodoroSummary> weekSummaryMap =
                calculateDailyPomodoroSummaryMap(memberId, weekStartDate, weekEndDate);

        int weeklyTotalFocusMinutes = 0;

        for (int i = 0; i < RECENT_DAYS; i++) {
            LocalDate currentDate = weekStartDate.plusDays(i);
            String currentApiDate = currentDate.format(API_DATE_FORMATTER);

            DailyPomodoroSummary summary =
                    weekSummaryMap.getOrDefault(currentApiDate, DailyPomodoroSummary.empty());

            weeklyTotalFocusMinutes += summary.totalFocusMinutes;
        }

        return new PomodoroStatsResponseDto(
                apiDate,
                todayTotalFocusMinutes,
                focusMinutesDiff,
                completedCount,
                completedCountDiff,
                averageFocusMinutes,
                averageFocusMinutesDiff,
                weeklyTotalFocusMinutes
        );
    }

    @Transactional
    public PomodoroDailyFocusResponseDto saveDailyFocus(Long memberId, PomodoroFocusSaveRequestDto request) {
        if (request.getDate() == null || request.getDate().isBlank()) {
            throw new IllegalArgumentException("date is required. format: yyyy-MM-dd");
        }

        if (request.getFocusLevel() == null) {
            throw new IllegalArgumentException("focusLevel is required.");
        }

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        LocalDate parsedDate = parseApiDate(request.getDate());
        String apiDate = parsedDate.format(API_DATE_FORMATTER);

        PomodoroDailyFocusEntity entity = dailyFocusRepository
                .findByMember_IdAndDate(memberId, apiDate)
                .orElseGet(PomodoroDailyFocusEntity::new);

        entity.setMember(member);
        entity.setDate(apiDate);
        entity.setFocusLevel(request.getFocusLevel());

        PomodoroDailyFocusEntity saved = dailyFocusRepository.save(entity);

        return new PomodoroDailyFocusResponseDto(saved.getDate(), saved.getFocusLevel());
    }

    public PomodoroRecentFocusListResponseDto getRecent7DaysFocus(Long memberId, String endDate) {
        LocalDate end = parseApiDate(endDate);
        LocalDate start = end.minusDays(RECENT_DAYS - 1);

        Map<String, DailyPomodoroSummary> summaryMap =
                calculateDailyPomodoroSummaryMap(memberId, start, end);

        List<PomodoroDailyFocusEntity> savedFocusList =
                dailyFocusRepository.findByMember_IdAndDateBetweenOrderByDateAsc(
                        memberId,
                        start.format(API_DATE_FORMATTER),
                        end.format(API_DATE_FORMATTER)
                );

        Map<String, PomodoroFocusLevel> focusLevelByDate = savedFocusList.stream()
                .collect(Collectors.toMap(
                        PomodoroDailyFocusEntity::getDate,
                        PomodoroDailyFocusEntity::getFocusLevel,
                        (oldValue, newValue) -> newValue
                ));

        List<PomodoroRecentFocusItemDto> items = new ArrayList<>();

        for (int i = 0; i < RECENT_DAYS; i++) {
            LocalDate currentDate = start.plusDays(i);
            String apiDate = currentDate.format(API_DATE_FORMATTER);

            DailyPomodoroSummary summary =
                    summaryMap.getOrDefault(apiDate, DailyPomodoroSummary.empty());

            PomodoroFocusLevel focusLevel = focusLevelByDate.get(apiDate);

            items.add(new PomodoroRecentFocusItemDto(
                    apiDate,
                    summary.totalFocusMinutes,
                    summary.completedCount,
                    summary.averageFocusMinutes,
                    focusLevel
            ));
        }

        return new PomodoroRecentFocusListResponseDto(items);
    }

    @Transactional
    public PomodoroTimerEntity addCompletedCount(Long memberId, Long timerId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0.");
        }

        PomodoroTimerEntity timer = repository.findByIdAndMember_Id(timerId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Pomodoro timer not found"));

        timer.setCompletedCount(timer.getCompletedCount() + count);

        PomodoroTimerEntity saved = repository.save(timer);

        publishPomodoroEvent(memberId, saved.getDate());

        return saved;
    }

    @Transactional
    public void deleteDailyFocus(Long memberId, String date) {
        LocalDate parsedDate = parseApiDate(date);
        String apiDate = parsedDate.format(API_DATE_FORMATTER);

        PomodoroDailyFocusEntity entity = dailyFocusRepository
                .findByMember_IdAndDate(memberId, apiDate)
                .orElseThrow(() -> new EntityNotFoundException("Daily focus data not found"));

        dailyFocusRepository.delete(entity);
    }

    @Transactional
    public PomodoroTimerEntity updateTimer(
            Long memberId,
            Long timerId,
            PomodoroTimerUpdateRequestDto request
    ) {
        PomodoroTimerEntity timer = repository.findByIdAndMember_Id(timerId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Pomodoro timer not found"));

        if (request.getTaskName() != null) {
            timer.setTaskName(request.getTaskName());
        }

        if (request.getFocusTime() != null) {
            if (request.getFocusTime() <= 0) {
                throw new IllegalArgumentException("focusTime must be greater than 0.");
            }

            timer.setFocusTime(request.getFocusTime());
        }

        if (request.getBreakTime() != null) {
            if (request.getBreakTime() < 0) {
                throw new IllegalArgumentException("breakTime must be 0 or greater.");
            }

            timer.setBreakTime(request.getBreakTime());
        }

        PomodoroTimerEntity saved = repository.save(timer);

        publishPomodoroEvent(memberId, saved.getDate());

        return saved;
    }

    private PomodoroTimerResponseDto convertToDto(PomodoroTimerEntity timer) {
        Long memberId = timer.getMember() != null ? timer.getMember().getId() : null;

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

    private DailyPomodoroSummary calculateDailyPomodoroSummary(Long memberId, LocalDate date) {
        String timerDateKey = toTimerDateKey(date);

        List<PomodoroTimerEntity> timers =
                repository.findByMember_IdAndDate(memberId, timerDateKey);

        return calculateSummaryFromTimers(timers);
    }

    private Map<String, DailyPomodoroSummary> calculateDailyPomodoroSummaryMap(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PomodoroTimerEntity> timers =
                repository.findByMember_IdAndDateBetween(
                        memberId,
                        toTimerDateKey(startDate),
                        toTimerDateKey(endDate)
                );

        Map<String, List<PomodoroTimerEntity>> groupedByDate =
                timers.stream().collect(Collectors.groupingBy(PomodoroTimerEntity::getDate));

        Map<String, DailyPomodoroSummary> result = new HashMap<>();

        for (Map.Entry<String, List<PomodoroTimerEntity>> entry : groupedByDate.entrySet()) {
            String apiDate = toApiDateFromTimerDateKey(entry.getKey());
            result.put(apiDate, calculateSummaryFromTimers(entry.getValue()));
        }

        return result;
    }

    private DailyPomodoroSummary calculateSummaryFromTimers(List<PomodoroTimerEntity> timers) {
        int totalFocusMinutes = timers.stream()
                .mapToInt(timer -> timer.getFocusTime() * timer.getCompletedCount())
                .sum();

        int completedCount = timers.stream()
                .mapToInt(PomodoroTimerEntity::getCompletedCount)
                .sum();

        return new DailyPomodoroSummary(totalFocusMinutes, completedCount);
    }

    private void publishPomodoroEvent(Long memberId, String timerDate) {
        if (memberId == null || timerDate == null || timerDate.isBlank()) return;

        LocalDate date = LocalDate.parse(timerDate, TIMER_DATE_FORMATTER);
        eventPublisher.publishEvent(new PomodoroCompletedEvent(memberId, date));
    }

    private LocalDate parseApiDate(String date) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("date is required. format: yyyy-MM-dd");
        }

        try {
            return LocalDate.parse(date, API_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
        }
    }

    private String toTimerDateKey(LocalDate date) {
        return date.format(TIMER_DATE_FORMATTER);
    }

    private String toApiDateFromTimerDateKey(String timerDateKey) {
        return LocalDate.parse(timerDateKey, TIMER_DATE_FORMATTER)
                .format(API_DATE_FORMATTER);
    }

    private static class DailyPomodoroSummary {
        private final int totalFocusMinutes;
        private final int completedCount;
        private final int averageFocusMinutes;

        private DailyPomodoroSummary(int totalFocusMinutes, int completedCount) {
            this.totalFocusMinutes = totalFocusMinutes;
            this.completedCount = completedCount;
            this.averageFocusMinutes = completedCount == 0
                    ? 0
                    : Math.round((float) totalFocusMinutes / completedCount);
        }

        private static DailyPomodoroSummary empty() {
            return new DailyPomodoroSummary(0, 0);
        }
    }
}