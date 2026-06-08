package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoRepository;
import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto.*;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
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

    private static final DateTimeFormatter API_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIMER_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    // 평소 평균 계산 기준: 오늘 제외 직전 30일
    private static final int BASELINE_DAYS = 30;

    // 최근 데이터 / 주간 누적 기준: 오늘 포함 최근 7일
    private static final int RECENT_DAYS = 7;

    public PomodoroTimerService(
            MemberRepository memberRepository,
            TodoRepository todoRepository,
            ScheduleCalendarService scheduleCalendarService,
            PomodoroTimerRepository repository,
            PomodoroDailyFocusRepository dailyFocusRepository
    ) {
        this.memberRepository = memberRepository;
        this.todoRepository = todoRepository;
        this.scheduleCalendarService = scheduleCalendarService;
        this.repository = repository;
        this.dailyFocusRepository = dailyFocusRepository;
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
     *
     * 요청 날짜 형식: yyyy-MM-dd
     * 저장 날짜 형식: yyyyMMdd
     *
     * 생성 시 CurrentTimer와 completedCount는 항상 0으로 고정합니다.
     */
    @Transactional
    public PomodoroTimerEntity create(PomodoroTimerDTO timerDto, Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        TodoEntity todo = null;

        if (timerDto.getTodoId() != null) {
            todo = todoRepository.findById(timerDto.getTodoId())
                    .orElseThrow(() -> new EntityNotFoundException("ToDo not found"));

            /*
             * 선택 사항:
             * 다른 사용자의 Todo에 포모도로가 연결되는 것을 막고 싶으면 사용하세요.
             *
             * if (todo.getMember() != null && !todo.getMember().getId().equals(memberId)) {
             *     throw new IllegalArgumentException("This ToDo does not belong to the current user.");
             * }
             */
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

        // 달력 저장 유지
        scheduleCalendarService.addOrUpdateEvent(
                memberId,
                formattedDate,
                "pomodoroTimer"
        );

        PomodoroTimerEntity pomodoroTimer = new PomodoroTimerEntity();
        pomodoroTimer.setMember(member);
        pomodoroTimer.setTodo(todo);
        pomodoroTimer.setDate(formattedDate);
        pomodoroTimer.setFocusTime(timerDto.getFocusTime());
        pomodoroTimer.setBreakTime(timerDto.getBreakTime());
        pomodoroTimer.setTaskName(timerDto.getTaskName());

        // 생성 시에는 무조건 0으로 고정
        pomodoroTimer.setCurrentTimer(0);
        pomodoroTimer.setCompletedCount(0);

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

    /** 전체 타이머 DTO 조회 */
    public List<PomodoroTimerResponseDto> getAllTimersAsDto() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 포모도로 통계 조회
     *
     * 통계는 pomodoro_daily_focus가 아니라 pomodoro_timer 원본 기록 기준으로 계산합니다.
     *
     * 계산식:
     * - 오늘 총 누적 집중 시간 = sum(focusTime * completedCount)
     * - 오늘 완료 횟수 = sum(completedCount)
     * - 오늘 평균 집중 시간 = 오늘 총 누적 집중 시간 / 오늘 완료 횟수
     * - focusMinutesDiff = 오늘 총 누적 집중 시간 - 직전 30일 하루 누적 집중 시간 평균
     * - completedCountDiff = 오늘 완료 횟수 - 직전 30일 하루 완료 횟수 평균
     * - averageFocusMinutesDiff = 오늘 평균 집중 시간 - 직전 30일 하루 평균 집중 시간 평균
     * - weeklyTotalFocusMinutes = 오늘 포함 최근 7일 총 누적 집중 시간
     */
    public PomodoroStatsResponseDto getPomodoroStats(Long memberId, String date) {
        LocalDate targetDate = parseApiDate(date);
        String apiDate = targetDate.format(API_DATE_FORMATTER);

        DailyPomodoroSummary todaySummary =
                calculateDailyPomodoroSummary(memberId, targetDate);

        int todayTotalFocusMinutes = todaySummary.totalFocusMinutes;
        int completedCount = todaySummary.completedCount;
        int averageFocusMinutes = todaySummary.averageFocusMinutes;

        /*
         * 평소 평균 기준
         * 오늘 제외 직전 30일
         */
        LocalDate baselineStartDate = targetDate.minusDays(BASELINE_DAYS);
        LocalDate baselineEndDate = targetDate.minusDays(1);

        Map<String, DailyPomodoroSummary> baselineSummaryMap =
                calculateDailyPomodoroSummaryMap(
                        memberId,
                        baselineStartDate,
                        baselineEndDate
                );

        int baselineTotalFocusMinutesSum = 0;
        int baselineCompletedCountSum = 0;
        int baselineAverageFocusMinutesSum = 0;

        for (int i = 0; i < BASELINE_DAYS; i++) {
            LocalDate currentDate = baselineStartDate.plusDays(i);
            String currentApiDate = currentDate.format(API_DATE_FORMATTER);

            DailyPomodoroSummary summary = baselineSummaryMap.getOrDefault(
                    currentApiDate,
                    DailyPomodoroSummary.empty()
            );

            baselineTotalFocusMinutesSum += summary.totalFocusMinutes;
            baselineCompletedCountSum += summary.completedCount;
            baselineAverageFocusMinutesSum += summary.averageFocusMinutes;
        }

        int baselineTotalFocusAverage =
                Math.round((float) baselineTotalFocusMinutesSum / BASELINE_DAYS);

        int focusMinutesDiff =
                todayTotalFocusMinutes - baselineTotalFocusAverage;

        int baselineCompletedCountAverage =
                Math.round((float) baselineCompletedCountSum / BASELINE_DAYS);

        int completedCountDiff =
                completedCount - baselineCompletedCountAverage;

        int baselineAverageFocusMinutes =
                Math.round((float) baselineAverageFocusMinutesSum / BASELINE_DAYS);

        int averageFocusMinutesDiff =
                averageFocusMinutes - baselineAverageFocusMinutes;

        /*
         * 주간 누적 집중 시간
         * 오늘 포함 최근 7일
         */
        LocalDate weekStartDate = targetDate.minusDays(RECENT_DAYS - 1);
        LocalDate weekEndDate = targetDate;

        Map<String, DailyPomodoroSummary> weekSummaryMap =
                calculateDailyPomodoroSummaryMap(
                        memberId,
                        weekStartDate,
                        weekEndDate
                );

        int weeklyTotalFocusMinutes = 0;

        for (int i = 0; i < RECENT_DAYS; i++) {
            LocalDate currentDate = weekStartDate.plusDays(i);
            String currentApiDate = currentDate.format(API_DATE_FORMATTER);

            DailyPomodoroSummary summary = weekSummaryMap.getOrDefault(
                    currentApiDate,
                    DailyPomodoroSummary.empty()
            );

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

    /**
     * 오늘의 집중도 저장/수정
     *
     * 이제 집중도만 저장합니다.
     * 시간, 완료 횟수, 평균 집중 시간은 저장하지 않습니다.
     */
    @Transactional
    public PomodoroDailyFocusResponseDto saveDailyFocus(
            Long memberId,
            PomodoroFocusSaveRequestDto request
    ) {
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

        return new PomodoroDailyFocusResponseDto(
                saved.getDate(),
                saved.getFocusLevel()
        );
    }

    /**
     * 최근 7일 집중 데이터 조회
     *
     * - totalFocusMinutes, completedCount, averageFocusMinutes는 pomodoro_timer 기준 계산
     * - focusLevel은 pomodoro_daily_focus에서 가져옴
     */
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

            DailyPomodoroSummary summary = summaryMap.getOrDefault(
                    apiDate,
                    DailyPomodoroSummary.empty()
            );

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

    /**
     * 포모도로 완료 횟수 추가
     */
    @Transactional
    public PomodoroTimerEntity addCompletedCount(Long memberId, Long timerId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0.");
        }

        PomodoroTimerEntity timer = repository.findByIdAndMember_Id(timerId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Pomodoro timer not found"));

        timer.setCompletedCount(timer.getCompletedCount() + count);

        return repository.save(timer);
    }

    /**
     * 오늘의 집중도 삭제
     */
    @Transactional
    public void deleteDailyFocus(Long memberId, String date) {
        LocalDate parsedDate = parseApiDate(date);
        String apiDate = parsedDate.format(API_DATE_FORMATTER);

        PomodoroDailyFocusEntity entity = dailyFocusRepository
                .findByMember_IdAndDate(memberId, apiDate)
                .orElseThrow(() -> new EntityNotFoundException("Daily focus data not found"));

        dailyFocusRepository.delete(entity);
    }

    /**
     * 포모도로 타이머 수정
     *
     * 수정 가능:
     * - taskName
     * - focusTime
     * - breakTime
     *
     * 수정 불가:
     * - date
     * - CurrentTimer
     * - completedCount
     * - member
     * - todo
     */
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

        return repository.save(timer);
    }

    private PomodoroTimerResponseDto convertToDto(PomodoroTimerEntity timer) {
        Long memberId = timer.getMember() != null
                ? timer.getMember().getId()
                : null;

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

    /**
     * 특정 날짜의 포모도로 통계 계산
     */
    private DailyPomodoroSummary calculateDailyPomodoroSummary(Long memberId, LocalDate date) {
        String timerDateKey = toTimerDateKey(date);

        List<PomodoroTimerEntity> timers =
                repository.findByMember_IdAndDate(memberId, timerDateKey);

        return calculateSummaryFromTimers(timers);
    }

    /**
     * 날짜 범위의 포모도로 통계 계산
     */
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

        Map<String, List<PomodoroTimerEntity>> groupedByDate = timers.stream()
                .collect(Collectors.groupingBy(PomodoroTimerEntity::getDate));

        Map<String, DailyPomodoroSummary> result = new HashMap<>();

        for (Map.Entry<String, List<PomodoroTimerEntity>> entry : groupedByDate.entrySet()) {
            String timerDateKey = entry.getKey();
            List<PomodoroTimerEntity> dayTimers = entry.getValue();

            String apiDate = toApiDateFromTimerDateKey(timerDateKey);

            result.put(apiDate, calculateSummaryFromTimers(dayTimers));
        }

        return result;
    }

    /**
     * 타이머 리스트에서 총 집중 시간, 완료 횟수, 평균 집중 시간 계산
     */
    private DailyPomodoroSummary calculateSummaryFromTimers(List<PomodoroTimerEntity> timers) {
        int totalFocusMinutes = timers.stream()
                .mapToInt(timer -> timer.getFocusTime() * timer.getCompletedCount())
                .sum();

        int completedCount = timers.stream()
                .mapToInt(PomodoroTimerEntity::getCompletedCount)
                .sum();

        return new DailyPomodoroSummary(totalFocusMinutes, completedCount);
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
        LocalDate date = LocalDate.parse(timerDateKey, TIMER_DATE_FORMATTER);
        return date.format(API_DATE_FORMATTER);
    }

    /**
     * 내부 계산용 클래스
     */
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