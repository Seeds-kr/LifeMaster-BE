package com.example.LifeMaster_BE.TimeManagerTest;

import com.example.LifeMaster_BE.TimeManager.PomodoroTimer.Dto.PomodoroTimerDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PomodoroTimerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String jwtToken;
    private Long createdTimerId;

    @BeforeEach
    void setUp() throws Exception {
        // 회원가입
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "test@example.com",
                          "password": "Test1234!",
                          "passwordConfirm": "Test1234!"
                        }
                        """)).andExpect(status().isOk());

        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "test@example.com",
                          "password": "Test1234!"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = loginResult.getResponse().getContentAsString().replace("\"", "");

        // 포모도로 생성
        PomodoroTimerDTO timerDTO = new PomodoroTimerDTO();
        timerDTO.setTaskName("공부");
        timerDTO.setCurrentTimer(0);
        timerDTO.setFocusTime(25);
        timerDTO.setBreakTime(5);
        timerDTO.setDate("20250711");

        MvcResult createResult = mockMvc.perform(post("/time/pomodoro/create")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timerDTO)))
                .andExpect(status().isOk())
                .andReturn();

        Map<?, ?> timerResponse = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        createdTimerId = ((Number) timerResponse.get("id")).longValue();
    }

    @Test
    @DisplayName("포모도로 타이머 전체 조회")
    void testGetAllTimers() throws Exception {
        mockMvc.perform(get("/time/pomodoro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskName").value("공부"));
    }

    @Test
    @DisplayName("ID로 포모도로 타이머 조회")
    void testGetTimerById() throws Exception {
        mockMvc.perform(get("/time/pomodoro/id/" + createdTimerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskName").value("공부"));
    }

    @Test
    @DisplayName("날짜로 포모도로 타이머 조회")
    void testGetTimerByDate() throws Exception {
        mockMvc.perform(get("/time/pomodoro/date/20250711"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskName").value("공부"));
    }

    @Test
    @DisplayName("ID로 포모도로 타이머 수정")
    void testUpdateTimer() throws Exception {
        String updateJson = """
            {
              "taskName": "수정된 작업",
              "currentTimer": 1,
              "focusTime": 30,
              "breakTime": 10,
              "date": "20250712"
            }
        """;

        mockMvc.perform(put("/time/pomodoro/" + createdTimerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskName").value("수정된 작업"))
                .andExpect(jsonPath("$.date").value("20250712"));
    }

    @Test
    @DisplayName("ID로 포모도로 타이머 삭제")
    void testDeleteTimerById() throws Exception {
        mockMvc.perform(delete("/time/pomodoro/id/" + createdTimerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/time/pomodoro/id/" + createdTimerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("날짜로 포모도로 전체 삭제")
    void testDeleteByDate() throws Exception {
        mockMvc.perform(delete("/time/pomodoro/date/20250711"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("비상 탈출 문장 생성 및 검증")
    void testEscapePhrase() throws Exception {
        // 1. 문장 생성
        MvcResult result = mockMvc.perform(get("/time/pomodoro/escape/generate"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String phrase = response.replace("Type this phrase to escape: ", "").trim();

        // 2. 검증 (정상)
        mockMvc.perform(post("/time/pomodoro/escape/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phrase\":\"" + phrase + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Escape successful! You are free."));

        // 3. 검증 (실패)
        mockMvc.perform(post("/time/pomodoro/escape/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phrase\":\"틀린 문장\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Escape failed! Try again."));
    }

    @Test
    @DisplayName("로그인된 회원의 포모도로 타이머 전체 조회")
    void testGetTimersByMember() throws Exception {
        mockMvc.perform(get("/time/pomodoro/member/1")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskName").value("공부"));
    }

    @Test
    @DisplayName("ToDo ID에 연결된 포모도로 삭제")
    void testDeleteByTodoId() throws Exception {
        // 연결된 todo가 없는 상태이므로 빈 동작으로 예상
        mockMvc.perform(delete("/time/pomodoro/todo/1"))
                .andExpect(status().isNoContent());
    }
}

