package com.example.LifeMaster_BE.TimeManagerTest;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.AlarmMissionController;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.AlarmMissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AlarmMissionController.class)
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AlarmMissionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AlarmMissionService missionService;

    // 🔧 메인 클래스(or 다른 빈)에서 요구하는 의존성으로 인한 컨텍스트 로딩 실패 방지
    @MockBean
    private com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoService todoService;

    @MockBean
    private com.example.LifeMaster_BE.Security.Utils.JwtUtil jwtUtil;

    @MockBean
    private com.example.LifeMaster_BE.Security.JwtAuthenticationFilter jwtAuthenticationFilter;

    // == 헬퍼: MathProblem 인스턴스 만들기 ==
    private AlarmMissionService.MathProblem mathProblem(String q, int a, String level) {
        return new AlarmMissionService.MathProblem(q, a, level);
    }

    // ===== 수학 문제 =====
    @Test
    @DisplayName("수학 문제 생성 - alarmId + level 파라미터 OK 응답")
    void generateMathProblem() throws Exception {
        long alarmId = 1L;
        var problem = mathProblem("3 + 4 = ?", 7, "상");

        given(missionService.generateMathProblem(alarmId, "상")).willReturn(problem);

        mockMvc.perform(get("/time/alarm/mission/math-problem")
                        .param("alarmId", String.valueOf(alarmId))
                        .param("level", "상"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));
        // 필요하면 아래처럼 값 검증도 가능 (게터 있으면)
        // .andExpect(jsonPath("$.question").value("3 + 4 = ?"))
        // .andExpect(jsonPath("$.correctAnswer").value(7));
    }

    // ===== 타이핑 =====
    @Test
    @DisplayName("랜덤 문장 생성 - alarmId 포함, 반환 문자열 확인")
    void generateTypingSentence() throws Exception {
        long alarmId = 10L;
        given(missionService.generateTypingSentence(alarmId)).willReturn("Wake up and shine!");

        mockMvc.perform(get("/time/alarm/mission/typing")
                        .param("alarmId", String.valueOf(alarmId)))
                .andExpect(status().isOk())
                .andExpect(content().string("Wake up and shine!"));
    }

    @Test
    @DisplayName("타이핑 정답 확인 - 성공 시 알람 끄기 호출")
    void checkTypingAnswer_success() throws Exception {
        long alarmId = 55L;

        given(missionService.checkTypingAnswer(alarmId, "Good morning!"))
                .willReturn("문장: \"Good morning!\"\n입력: \"Good morning!\" (성공! 알람이 꺼졌습니다.)");

        mockMvc.perform(post("/time/alarm/mission/typing/check")
                        .param("userInput", "Good morning!")
                        .param("alarmId", String.valueOf(alarmId)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("성공!")));

        verify(missionService).updateAlarmStatus(alarmId, false);
    }

    // ===== 따라 누르기 (5x5 그리드) =====
    @Test
    @DisplayName("그리드 생성 - alarmId + level, 5x5 배열 JSON 반환")
    void generateFollowClickGrid() throws Exception {
        long alarmId = 3L;
        int[][] grid = new int[5][5];
        grid[0][0] = 1; grid[1][1] = 1;

        given(missionService.generateFollowClickGrid(alarmId, "중")).willReturn(grid);

        mockMvc.perform(get("/time/alarm/mission/follow-click")
                        .param("alarmId", String.valueOf(alarmId))
                        .param("level", "중"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0]", hasSize(5)))
                .andExpect(jsonPath("$[0][0]").value(1));
    }

    @Test
    @DisplayName("그리드 정답 확인 - 정답 시 알람 끄기 호출")
    void checkFollowClickAnswer_correct() throws Exception {
        long alarmId = 777L;

        int[][] userGrid = new int[5][5];
        userGrid[2][2] = 1;

        given(missionService.checkFollowClickAnswer(eq(alarmId), any(int[][].class)))
                .willReturn("정답입니다! 알람이 꺼졌습니다.");

        mockMvc.perform(post("/time/alarm/mission/follow-click/check")
                        .param("alarmId", String.valueOf(alarmId))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userGrid)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("정답입니다!")));

        verify(missionService).updateAlarmStatus(alarmId, false);
    }


}
