package com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            summary = "알람 미션 문제/정답 조회(정답 확인용)",
            description = """
    alarmId로 해당 알람의 미션 타입을 확인하고,
    - 본인(memberId)이면 문제/정답(또는 미션 데이터)을 반환합니다.
    - 본인 알람이 아니면 '아이디가 다르다'로 반환합니다.
    - alarmId가 없으면 '없다'로 반환합니다.
    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공(본인 알람)"),
            @ApiResponse(responseCode = "403", description = "본인 알람 아님"),
            @ApiResponse(responseCode = "404", description = "alarmId 없음")
    })
    @GetMapping("/{alarmId}/mission-answer")
    public ResponseEntity<?> getMissionAnswer(
            @PathVariable Long alarmId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        try {
            AlarmMissionAnswerResponseDto dto =
                    missionService.getMissionQuestionAndAnswer(alarmId, memberId);

            return ResponseEntity.ok(dto);

        } catch (EntityNotFoundException e) {
            // alarmId 없음
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", 404,
                            "message", "해당 알람이 없습니다.",
                            "alarmId", alarmId
                    ));

        } catch (SecurityException e) {
            // memberId 불일치
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", 403,
                            "message", "아이디가 다릅니다.",
                            "alarmId", alarmId
                    ));
        }
    }
}
