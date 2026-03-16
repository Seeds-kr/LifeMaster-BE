package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Pomodoro Timer API", description = "포모도로 타이머 관리 API")
@RestController
@RequestMapping("/time/pomodoro")
public class PomodoroTimerController {

    @Autowired
    private PomodoroTimerService service;

    @Autowired
    private ScheduleCalendarService calendarService;

    private String currentEscapePhrase;

    private final Login login;

    public PomodoroTimerController(Login login) {
        this.login = login;
    }

    @Operation(summary = "모든 포모도로 타이머 조회", description = "저장된 모든 포모도로 타이머를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<PomodoroTimerResponseDto>> getAllTimers() {
        return ResponseEntity.ok(service.getAllTimersAsDto());
    }

    @Operation(summary = "ID로 특정 포모도로 타이머 조회", description = "ID를 사용해 특정 포모도로 타이머를 조회합니다.")
    @GetMapping("/id/{id}")
    public ResponseEntity<PomodoroTimerEntity> getTimerById(@PathVariable(name = "id") Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date/{date}")
    public List<PomodoroTimerResponseDto> getTimerByDate(@PathVariable(name = "date") String date) {
        return service.findByDate(date);
    }

    @Operation(summary = "새로운 포모도로 타이머 생성",
            description = "새로운 포모도로 타이머를 생성하고, 캘린더에 관련 항목을 추가합니다.")
    @PostMapping("/create")
    public ResponseEntity<?> createTimer(@RequestBody PomodoroTimerDTO timer, @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        Long memberId = user.getId();
        PomodoroTimerEntity pomodoroTimerEntity = service.create(timer,memberId);

        return ResponseEntity.ok(pomodoroTimerEntity);
    }

    @Operation(summary = "ID로 특정 포모도로 타이머 업데이트",
            description = "ID를 사용해 특정 포모도로 타이머의 정보를 업데이트합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<PomodoroTimerEntity> updateTimer(@PathVariable(name = "id") Long id, @RequestBody PomodoroTimerEntity timerDetails) {
        return service.findById(id).map(timer -> {
            timer.setTaskName(timerDetails.getTaskName());
            timer.setCurrentTimer(timerDetails.getCurrentTimer());
            timer.setFocusTime(timerDetails.getFocusTime());
            timer.setBreakTime(timerDetails.getBreakTime());
            timer.setDate(timerDetails.getDate());
            return ResponseEntity.ok(service.save(timer));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "ID로 특정 포모도로 타이머 삭제",
            description = "ID를 사용해 특정 포모도로 타이머를 삭제하고 캘린더에서 해당 항목도 제거합니다.")
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteTimerById(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        Optional<PomodoroTimerEntity> timerOpt = service.findById(id);
        if (timerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PomodoroTimerEntity timer = timerOpt.get();

        // ✅ (권장) 내 것만 삭제 보장: 엔티티에 memberId/Member가 있다면 체크
        // if (!timer.getMember().getId().equals(memberId)) return ResponseEntity.status(403).build();

        String dateKey = timer.getDate(); // yyyyMMdd라고 가정
        calendarService.deleteSpecificEvent(memberId, dateKey, "pomodoroTimer");

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "날짜로 모든 포모도로 타이머 삭제",
            description = "입력된 날짜(YYYYMMDD)에 해당하는 모든 포모도로 타이머를 삭제합니다.")
    @DeleteMapping("/date/{date}")
    public ResponseEntity<Void> deleteTimerByDate(@PathVariable(name = "date") String date) {
        service.deleteAllByDate(date);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "비상 탈출 문장 생성",
            description = "사용자가 입력해야 할 비상 탈출 문장을 생성합니다.")
    @GetMapping("/escape/generate")
    public String generateEscapePhrase() {
        currentEscapePhrase = EscapePhrases.getRandomPhrase();
        return "Type this phrase to escape: " + currentEscapePhrase;
    }

    @Operation(summary = "비상 탈출 문장 검증",
            description = "사용자가 입력한 비상 탈출 문장이 정확한지 검증합니다.")
    @PostMapping("/escape/verify")
    public ResponseEntity<String> verifyEscapePhrase(@RequestBody EscapePhraseRequest request) {
        String userInput = request.getPhrase();
        if (currentEscapePhrase != null && currentEscapePhrase.equals(userInput)) {
            currentEscapePhrase = null;
            return ResponseEntity.ok("Escape successful! You are free.");
        } else {
            return ResponseEntity.status(403).body("Escape failed! Try again.");
        }
    }

    /**
     * 🔍 로그인된 사용자의 모든 포모도로 타이머 기록을 조회합니다.
     * - 사용자는 인증이 되어 있어야 하며, `@AuthenticationPrincipal`을 통해 본인의 ID로 자동 조회됩니다.
     * - URI의 {memberId}는 사용되지 않으며, 인증된 사용자 정보를 우선합니다.
     *
     * @param user 현재 인증된 사용자 (Spring Security에서 주입됨)
     * @return 해당 사용자의 모든 포모도로 타이머 리스트
     */
    @Operation(summary = "회원 전체 포모도로 타이머 조회",
            description = "로그인된 회원의 모든 포모도로 타이머 기록을 조회합니다.")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getByMember(@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        return ResponseEntity.ok(service.getTimersByMember(user.getId()));
    }

    /**
     * 🔍 특정 회원이 특정 ToDo 항목에 대해 수행한 모든 포모도로 타이머 기록을 조회합니다.
     * 사용자는 로그인 상태여야 하며, 자신의 ID를 기반으로 자동 조회됩니다.
     *
     * @param user 현재 인증된 사용자 (Spring Security에서 주입)
     * @param todoId 조회할 대상 ToDo 항목의 ID
     * @return 해당 회원의 특정 ToDo에 연결된 포모도로 타이머 리스트
     */
    @Operation(summary = "회원 + 특정 ToDo에 대한 포모도로 타이머 조회",
            description = "로그인된 회원이 특정 ToDo ID에 대해 생성한 모든 포모도로 타이머 기록을 반환합니다.")
    @GetMapping("/member/{memberId}/{todoId}")
    public ResponseEntity<?> getByMemberAndTodo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable(name = "todoId") Long todoId) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        return ResponseEntity.ok(service.getTimersByMemberAndTodo(user.getId(), todoId));
    }

    @Operation(summary = "특정 ToDo ID에 연결된 포모도로 타이머 전체 삭제",
            description = "ToDo ID로 연결된 모든 포모도로 타이머를 삭제합니다.")
    @DeleteMapping("/todo/{todoId}")
    public ResponseEntity<Void> deleteByTodoId(@PathVariable(name = "todoId") Long todoId) {
        service.deleteAllByTodoId(todoId);
        return ResponseEntity.noContent().build();
    }

}
