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

    public AlarmMissionController(AlarmMissionService missionService) {
        this.missionService = missionService;
    }

    // ========== 수학 문제 ==========

    @Operation(
            summary = "수학 문제 생성",
            description = "주어진 난이도와 알람 ID에 맞는 수학 문제를 생성하고, 알람에 저장합니다."
    )
    @Parameter(name = "alarmId", description = "문제가 연결될 알람 ID", required = true)
    @Parameter(name = "level", description = "문제 난이도 (상, 중, 하)", required = true)
    @GetMapping("/math-problem")
    public ResponseEntity<AlarmMissionService.MathProblem> generateMathProblem(
            @RequestParam(name = "alarmId") Long alarmId,
            @RequestParam(name = "level") String level
    ) {
        AlarmMissionService.MathProblem problem = missionService.generateMathProblem(alarmId, level);
        return ResponseEntity.ok(problem);
    }

    @Operation(
            summary = "수학 문제 정답 확인",
            description = "알람에 저장된 수학 문제에 대해, 사용자가 입력한 답이 맞는지 확인합니다."
    )
    @PostMapping("/math-problem/check")
    public ResponseEntity<String> checkMathProblemAnswer(
            @RequestParam(name = "alarmId") Long alarmId,
            @RequestParam(name = "answer") int answer
    ) {
        String result = missionService.checkMathProblemAnswer(alarmId, answer);

        // 정답이면 알람 끄기 (옵션)
        if (result.contains("정답입니다!")) {
            missionService.updateAlarmStatus(alarmId, false);
        }

        return ResponseEntity.ok(result);
    }

    // ========== 문장 따라쓰기 ==========

    @Operation(
            summary = "랜덤 문장 생성",
            description = "알람 ID에 연결된 랜덤 문장을 생성하고 알람에 저장합니다."
    )
    @Parameter(name = "alarmId", description = "문장이 연결될 알람 ID", required = true)
    @GetMapping("/typing")
    public ResponseEntity<String> generateTypingSentence(
            @RequestParam(name = "alarmId") long alarmId
    ) {
        String sentence = missionService.generateTypingSentence(alarmId);
        return ResponseEntity.ok(sentence);
    }

    @Operation(
            summary = "문장 정답 확인",
            description = "알람에 저장된 문장과 사용자가 입력한 문장이 일치하는지 확인합니다."
    )
    @Parameter(name = "userInput", description = "사용자가 입력한 문장", required = true)
    @Parameter(name = "alarmId", description = "알람 ID", required = true)
    @PostMapping("/typing/check")
    public ResponseEntity<String> checkTypingAnswer(
            @RequestParam(name = "userInput") String userInput,
            @RequestParam(name = "alarmId") long alarmId
    ) {
        String result = missionService.checkTypingAnswer(alarmId, userInput);

        if (result.contains("성공!")) {
            missionService.updateAlarmStatus(alarmId, false); // 알람 끄기
        }

        return ResponseEntity.ok(result);
    }

    // ========== 따라 누르기 ==========

    @Operation(
            summary = "5x5 클릭 그리드 생성",
            description = "주어진 난이도와 알람 ID에 맞는 5x5 클릭 그리드를 생성하고 알람에 저장합니다."
    )
    @Parameter(name = "alarmId", description = "그리드가 연결될 알람 ID", required = true)
    @Parameter(name = "level", description = "그리드 생성 난이도 (상, 중, 하)", required = true)
    @GetMapping("/follow-click")
    public ResponseEntity<int[][]> generateFollowClickGrid(
            @RequestParam(name = "alarmId") long alarmId,
            @RequestParam(name = "level") String level
    ) {
        int[][] grid = missionService.generateFollowClickGrid(alarmId, level);
        return ResponseEntity.ok(grid);
    }

    @Operation(
            summary = "그리드 정답 확인",
            description = "알람에 저장된 정답 그리드와 사용자가 입력한 그리드를 비교하여 결과를 반환합니다."
    )
    @Parameter(name = "alarmId", description = "알람 ID", required = true)
    @PostMapping("/follow-click/check")
    public ResponseEntity<String> checkFollowClickAnswer(
            @RequestBody int[][] userGrid,
            @RequestParam(name = "alarmId") long alarmId
    ) {
        String result = missionService.checkFollowClickAnswer(alarmId, userGrid);

        if (result.contains("정답입니다!")) {
            missionService.updateAlarmStatus(alarmId, false); // 알람 끄기
        }

        return ResponseEntity.ok(result);
    }
}
