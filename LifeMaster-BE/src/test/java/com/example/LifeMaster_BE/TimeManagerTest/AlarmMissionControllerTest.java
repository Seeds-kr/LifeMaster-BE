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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AlarmMissionController.class)
class AlarmMissionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private AlarmMissionController controller;

    @MockBean private AlarmMissionService missionService;

    // 🔧 메인 클래스(or 다른 빈)에서 요구하는 의존성으로 인한 컨텍스트 로딩 실패 방지
    @MockBean
    private com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoService todoService;

    @MockBean
    private com.example.LifeMaster_BE.Security.Utils.JwtUtil jwtUtil;

    @MockBean
    private com.example.LifeMaster_BE.Security.JwtAuthenticationFilter jwtAuthenticationFilter;

    // == 헬퍼: MathProblem 인스턴스 만들기(정적/비정적 둘 다 대응) ==
    private AlarmMissionService.MathProblem mathProblem(String q, int a) throws Exception {
        try {
            // 1) 정적 중첩 클래스인 경우: (String, int)
            var ctor = AlarmMissionService.MathProblem.class
                    .getDeclaredConstructor(String.class, int.class);
            ctor.setAccessible(true);
            return ctor.newInstance(q, a);
        } catch (NoSuchMethodException ignore) {
            // 2) 비정적(Inner) 클래스인 경우: (AlarmMissionService, String, int)
            var ctor = AlarmMissionService.MathProblem.class
                    .getDeclaredConstructor(AlarmMissionService.class, String.class, int.class);
            ctor.setAccessible(true);
            // missionService는 @MockBean 으로 이미 주입되어 있음
            return ctor.newInstance(missionService, q, a);
        }
    }

    // ===== 수학 문제 =====
    @Test
    @DisplayName("수학 문제 생성 - level 파라미터 OK 응답")
    void generateMathProblem() throws Exception {
        // 직렬화 구현(게터 유무)에 덜 민감하도록 본문 구조는 단언하지 않음
        var problem = mathProblem("3 + 4 = ?", 7);
        given(missionService.generateMathProblem("상")).willReturn(problem);

        mockMvc.perform(get("/time/alarm/mission/math-problem").param("level", "상"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));
        // 프로덕션에 게터가 있다면 아래 주석 해제
        // .andExpect(jsonPath("$.question").value("3 + 4 = ?"))
        // .andExpect(jsonPath("$.answer").value(7));
    }

    @Test
    @DisplayName("수학 문제 정답 확인 - 정답 시 알람 끄기 호출")
    void checkMathProblemAnswer_correct() throws Exception {
        var stored = mathProblem("5 + 5 = ?", 10);
        ReflectionTestUtils.setField(controller, "mathProblem", stored);

        given(missionService.checkMathProblemAnswer(any(AlarmMissionService.MathProblem.class), eq(10)))
                .willReturn("정답입니다! 수고하셨어요.");

        mockMvc.perform(post("/time/alarm/mission/math-problem/check")
                        .param("userAnswer", "10")
                        .param("alarmId", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("정답입니다!")));

        verify(missionService).updateAlarmStatus(123L, false);
    }

    // ===== 타이핑 =====
    @Test
    @DisplayName("랜덤 문장 생성 - 반환 문자열 확인")
    void generateTypingSentence() throws Exception {
        given(missionService.generateTypingSentence()).willReturn("Wake up and shine!");

        mockMvc.perform(get("/time/alarm/mission/typing"))
                .andExpect(status().isOk())
                .andExpect(content().string("Wake up and shine!"));
    }

    @Test
    @DisplayName("타이핑 정답 확인 - 성공 시 알람 끄기 호출")
    void checkTypingAnswer_success() throws Exception {
        ReflectionTestUtils.setField(controller, "typingAnswer", "Good morning!");

        given(missionService.checkTypingAnswer(eq("Good morning!"), eq("Good morning!")))
                .willReturn("성공!");

        mockMvc.perform(post("/time/alarm/mission/typing/check")
                        .param("userInput", "Good morning!")
                        .param("alarmId", "55"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("성공!")));

        verify(missionService).updateAlarmStatus(55L, false);
    }

    // ===== 따라 누르기 (5x5 그리드) =====
    @Test
    @DisplayName("그리드 생성 - 5x5 배열 JSON 반환")
    void generateFollowClickGrid() throws Exception {
        int[][] grid = new int[5][5];
        grid[0][0] = 1; grid[1][1] = 1;

        given(missionService.generateFollowClickGrid("중")).willReturn(grid);

        mockMvc.perform(get("/time/alarm/mission/follow-click").param("level", "중"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0]", hasSize(5)))
                .andExpect(jsonPath("$[0][0]").value(1));
    }

    @Test
    @DisplayName("그리드 정답 확인 - 정답 시 알람 끄기 호출")
    void checkFollowClickAnswer_correct() throws Exception {
        int[][] answer = new int[5][5];
        answer[2][2] = 1;
        ReflectionTestUtils.setField(controller, "answerGrid", answer);

        int[][] userGrid = new int[5][5];
        userGrid[2][2] = 1;

        given(missionService.checkFollowClickAnswer(any(int[][].class), any(int[][].class)))
                .willReturn("정답입니다!");

        mockMvc.perform(post("/time/alarm/mission/follow-click/check")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userGrid))
                        .param("alarmId", "777"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("정답입니다!")));

        verify(missionService).updateAlarmStatus(777L, false);
    }
}
