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
import java.time.ZoneId;
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

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

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
     * 날짜는 한국 시간 기준으로 저장됩니다.
     * 저장 형식: yyyyMMdd
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

        LocalDate today = LocalDate.now(KOREA_ZONE_ID);
        String formattedDate = today.format(TIMER_DATE_FORMATTER);

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
     * 요청 date 형식: yyyy-MM-dd
     *
     * 계산 기준:
     * - 오늘 총 집중 시간: 해당 날짜 포모도로 focusTime 합계
     * - 오늘의 집중도: PomodoroDailyFocusEntity에 저장된 focusLevel
     * - 평소보다 더 집중한 시간: 오늘 총 집중 시간 - 직전 30일 하루 평균 집중 시간
     * - 완료한 뽀모도로 횟수: 해당 날짜 포모도로 기록 개수
     * - 완료 횟수 차이: 오늘 완료 횟수 - 직전 30일 하루 평균 완료 횟수
     * - 평균 집중 시간: 오늘 총 집중 시간 / 오늘 완료 횟수
     * - 평균 집중 시간 차이: 오늘 평균 집중 시간 - 직전 30일 포모도로 1회당 평균 집중 시간
     * - 주간 누적 집중 시간: 오늘 포함 최근 7일 focusTime 합계
     */
    public PomodoroStatsResponseDto getPomodoroStats(Long memberId, String date) {
        LocalDate targetDate = parseApiDate(date);
        String apiDate = targetDate.format(API_DATE_FORMATTER);

        PomodoroDailyFocusEntity todayFocus =
                dailyFocusRepository.findByMember_IdAndDate(memberId, apiDate)
                        .orElse(null);

        int todayTotalFocusMinutes = todayFocus != null
                ? todayFocus.getTotalFocusMinutes()
                : 0;

        int completedCount = todayFocus != null
                ? todayFocus.getCompletedCount()
                : 0;

        int averageFocusMinutes = todayFocus != null
                ? todayFocus.getAverageFocusMinutes()
                : 0;

        PomodoroFocusLevel focusLevel = todayFocus != null
                ? todayFocus.getFocusLevel()
                : null;

        /*
         * 평소 평균 기준
         * 오늘 제외 직전 30일
         */
        LocalDate baselineStartDate = targetDate.minusDays(BASELINE_DAYS);
        LocalDate baselineEndDate = targetDate.minusDays(1);

        List<PomodoroDailyFocusEntity> baselineFocusList =
                dailyFocusRepository.findByMember_IdAndDateBetweenOrderByDateAsc(
                        memberId,
                        baselineStartDate.format(API_DATE_FORMATTER),
                        baselineEndDate.format(API_DATE_FORMATTER)
                );

        int baselineTotalFocusMinutes = baselineFocusList.stream()
                .mapToInt(PomodoroDailyFocusEntity::getTotalFocusMinutes)
                .sum();

        int baselineTotalFocusAverage =
                Math.round((float) baselineTotalFocusMinutes / BASELINE_DAYS);

        int focusMinutesDiff =
                todayTotalFocusMinutes - baselineTotalFocusAverage;

        int baselineCompletedCountTotal = baselineFocusList.stream()
                .mapToInt(PomodoroDailyFocusEntity::getCompletedCount)
                .sum();

        int baselineCompletedCountAverage =
                Math.round((float) baselineCompletedCountTotal / BASELINE_DAYS);

        int completedCountDiff =
                completedCount - baselineCompletedCountAverage;

        /*
         * 30일간의 집중 시간 평균
         * 누적 집중 시간 평균이 아니라,
         * daily_focus.averageFocusMinutes의 30일 평균
         */
        int baselineAverageFocusMinutesTotal = baselineFocusList.stream()
                .mapToInt(PomodoroDailyFocusEntity::getAverageFocusMinutes)
                .sum();

        int baselineAverageFocusMinutes =
                Math.round((float) baselineAverageFocusMinutesTotal / BASELINE_DAYS);

        int averageFocusMinutesDiff =
                averageFocusMinutes - baselineAverageFocusMinutes;

        /*
         * 주간 누적 집중 시간
         * 오늘 포함 최근 7일
         */
        LocalDate weekStartDate = targetDate.minusDays(RECENT_DAYS - 1);
        LocalDate weekEndDate = targetDate;

        List<PomodoroDailyFocusEntity> weeklyFocusList =
                dailyFocusRepository.findByMember_IdAndDateBetweenOrderByDateAsc(
                        memberId,
                        weekStartDate.format(API_DATE_FORMATTER),
                        weekEndDate.format(API_DATE_FORMATTER)
                );

        int weeklyTotalFocusMinutes = weeklyFocusList.stream()
                .mapToInt(PomodoroDailyFocusEntity::getTotalFocusMinutes)
                .sum();

        return new PomodoroStatsResponseDto(
                apiDate,
                todayTotalFocusMinutes,
                focusMinutesDiff,
                completedCount,
                completedCountDiff,
                averageFocusMinutes,
                averageFocusMinutesDiff,
                weeklyTotalFocusMinutes,
                focusLevel
        );
    }

    /**
     * 오늘의 집중도 저장/수정
     *
     * 같은 memberId + date 데이터가 있으면 수정,
     * 없으면 새로 생성합니다.
     *
     * 요청 date 형식: yyyy-MM-dd
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
        String timerDateKey = toTimerDateKey(parsedDate);

        List<PomodoroTimerEntity> todayTimers =
                repository.findByMember_IdAndDate(memberId, timerDateKey);

        int totalFocusMinutes = todayTimers.stream()
                .mapToInt(PomodoroTimerEntity::getFocusTime)
                .sum();

        int completedCount = todayTimers.size();

        int averageFocusMinutes = completedCount == 0
                ? 0
                : Math.round((float) totalFocusMinutes / completedCount);

        PomodoroDailyFocusEntity entity = dailyFocusRepository
                .findByMember_IdAndDate(memberId, apiDate)
                .orElseGet(PomodoroDailyFocusEntity::new);

        entity.setMember(member);
        entity.setDate(apiDate);
        entity.setFocusLevel(request.getFocusLevel());

        // 서버가 PomodoroTimerEntity 기준으로 자동 계산해서 저장
        entity.setTotalFocusMinutes(totalFocusMinutes);
        entity.setCompletedCount(completedCount);
        entity.setAverageFocusMinutes(averageFocusMinutes);

        PomodoroDailyFocusEntity saved = dailyFocusRepository.save(entity);

        return new PomodoroDailyFocusResponseDto(
                saved.getDate(),
                saved.getTotalFocusMinutes(),
                saved.getCompletedCount(),
                saved.getAverageFocusMinutes(),
                saved.getFocusLevel()
        );
    }

    /**
     * 최근 7일 집중 데이터 조회
     *
     * endDate 요청 형식: yyyy-MM-dd
     * 반환 범위: endDate 포함 최근 7일
     *
     * totalFocusMinutes는 실제 PomodoroTimerEntity 기록 기준으로 계산합니다.
     * focusLevel은 저장된 값이 없으면 null입니다.
     */
    public PomodoroRecentFocusListResponseDto getRecent7DaysFocus(Long memberId, String endDate) {
        LocalDate end = parseApiDate(endDate);
        LocalDate start = end.minusDays(RECENT_DAYS - 1);

        List<PomodoroDailyFocusEntity> savedFocusList =
                dailyFocusRepository.findByMember_IdAndDateBetweenOrderByDateAsc(
                        memberId,
                        start.format(API_DATE_FORMATTER),
                        end.format(API_DATE_FORMATTER)
                );

        Map<String, PomodoroDailyFocusEntity> focusByDate = savedFocusList.stream()
                .collect(Collectors.toMap(
                        PomodoroDailyFocusEntity::getDate,
                        focus -> focus,
                        (oldValue, newValue) -> newValue
                ));

        List<PomodoroDailyFocusResponseDto> items = new ArrayList<>();

        for (int i = 0; i < RECENT_DAYS; i++) {
            LocalDate currentDate = start.plusDays(i);
            String apiDate = currentDate.format(API_DATE_FORMATTER);

            PomodoroDailyFocusEntity focus = focusByDate.get(apiDate);

            int totalFocusMinutes = focus != null
                    ? focus.getTotalFocusMinutes()
                    : 0;

            int completedCount = focus != null
                    ? focus.getCompletedCount()
                    : 0;

            int averageFocusMinutes = focus != null
                    ? focus.getAverageFocusMinutes()
                    : 0;

            PomodoroFocusLevel focusLevel = focus != null
                    ? focus.getFocusLevel()
                    : null;

            items.add(new PomodoroDailyFocusResponseDto(
                    apiDate,
                    totalFocusMinutes,
                    completedCount,
                    averageFocusMinutes,
                    focusLevel
            ));
        }

        return new PomodoroRecentFocusListResponseDto(items);
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

    @Transactional
    public void deleteDailyFocus(Long memberId, String date) {
        LocalDate parsedDate = parseApiDate(date);
        String apiDate = parsedDate.format(API_DATE_FORMATTER);

        PomodoroDailyFocusEntity entity = dailyFocusRepository
                .findByMember_IdAndDate(memberId, apiDate)
                .orElseThrow(() -> new EntityNotFoundException("Daily focus data not found"));

        dailyFocusRepository.delete(entity);
    }
}