package com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alarm Mission API", description = "알람 미션 관련 API")
@RestController
@RequestMapping("/time/alarm/mission")
public class AlarmMissionController {

    private final AlarmMissionService missionService;

    private String typingAnswer;
    private AlarmMissionService.MathProblem mathProblem;

    private int[][] answerGrid;

    public AlarmMissionController(AlarmMissionService missionService) {
        this.missionService = missionService;
    }

    // ========== 수학 문제 ==========

    @Operation(summary = "수학 문제 생성", description = "주어진 난이도에 맞는 수학 문제를 생성합니다.")
    @Parameter(name = "level", description = "문제 난이도 (상, 중, 하)", required = true)
    @GetMapping("/math-problem")
    public ResponseEntity<AlarmMissionService.MathProblem> generateMathProblem(@RequestParam(name = "level") String level) {
        AlarmMissionService.MathProblem problem = missionService.generateMathProblem(level);
        mathProblem = problem;
        return ResponseEntity.ok(problem);
    }

    @Operation(summary = "수학 문제 정답 확인", description = "사용자가 입력한 정답을 확인하고 결과를 반환합니다.")
    @Parameter(name = "userAnswer", description = "사용자가 입력한 정답", required = true)
    @PostMapping("/math-problem/check")
    public ResponseEntity<String> checkMathProblemAnswer(
            @RequestParam(name = "userAnswer") int userAnswer,
            @RequestParam(name = "alarmId") long alarmId // 알람 ID 추가
    ) {
        String result = missionService.checkMathProblemAnswer(mathProblem, userAnswer);
        // 정답이 맞으면 알람 상태 업데이트
        if (result.contains("정답입니다!")) {
            missionService.updateAlarmStatus(alarmId, false); // 알람 끄기
            mathProblem = null;
        }
        return ResponseEntity.ok(result);
    }

    // ========== 문장 따라쓰기 ==========

    @Operation(summary = "랜덤 문장 생성", description = "랜덤한 문장을 생성하여 반환합니다.")
    @GetMapping("/typing")
    public ResponseEntity<String> generateTypingSentence() {
        String sentence = missionService.generateTypingSentence();
        typingAnswer = sentence;
        return ResponseEntity.ok(sentence);
    }

    @Operation(summary = "문장 정답 확인", description = "사용자가 입력한 문장이 생성된 문장과 일치하는지 확인합니다.")
    @Parameter(name = "userInput", description = "사용자가 입력한 문장", required = true)
    @PostMapping("/typing/check")
    public ResponseEntity<String> checkTypingAnswer(
            @RequestParam(name = "userInput") String userInput,
            @RequestParam(name = "alarmId") long alarmId // 알람 ID 추가
    ) {
        String result = missionService.checkTypingAnswer(typingAnswer, userInput);
        // 정답이 맞으면 typingAnswer 초기화
        if (result.contains("성공!")) {
            missionService.updateAlarmStatus(alarmId, false); // 알람 끄기
            typingAnswer = null;
        }
        return ResponseEntity.ok(result);
    }

    // ========== 따라 누르기 ==========

    @Operation(summary = "5x5 클릭 그리드 생성", description = "주어진 난이도에 맞는 5x5 클릭 그리드를 생성합니다.")
    @Parameter(name = "level", description = "그리드 생성 난이도 (상, 중, 하)", required = true)
    @GetMapping("/follow-click")
    public ResponseEntity<int[][]> generateFollowClickGrid(@RequestParam(name = "level") String level) {
        int[][] grid = missionService.generateFollowClickGrid(level);
        answerGrid = grid;
        return ResponseEntity.ok(grid);
    }

    @Operation(summary = "그리드 정답 확인", description = "사용자가 입력한 그리드와 생성된 그리드를 비교하여 결과를 반환합니다.")
    @PostMapping("/follow-click/check")
    public ResponseEntity<String> checkFollowClickAnswer(
            @RequestBody int[][] userGrid,
            @RequestParam(name = "alarmId") long alarmId // 알람 ID 추가
    ) {
        String result = missionService.checkFollowClickAnswer(answerGrid, userGrid);
        // 정답이 맞으면 mathProblem 초기화
        if (result.contains("정답입니다!")) {
            missionService.updateAlarmStatus(alarmId, false); // 알람 끄기
            answerGrid = null;
        }
        return ResponseEntity.ok(result);
    }
}
