package com.example.LifeMaster_BE.Challenge;

import com.fasterxml.jackson.databind.JsonNode;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TimeDetoxControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String jwtToken;
    private Long scheduleId;

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
                        """))
                .andExpect(status().isOk());

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

        // 디톡스 일정 생성 -> 응답에서 id 추출
        MvcResult createResult = mockMvc.perform(post("/detox/time")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "cycle": "WEEKLY",
                                "day": "MONDAY",
                                "startTime": "10:30:00",
                                "endTime": "18:30:00",
                                "active": true,
                                "lockedApps": ["YouTube", "Instagram"]
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        JsonNode node = objectMapper.readTree(createResult.getResponse().getContentAsString());
        scheduleId = node.get("id").asLong();
    }

    @Test
    @DisplayName("디톡스 일정 생성 후 전체 조회")
    void testGetAllDetoxSchedules() throws Exception {
        mockMvc.perform(get("/detox/time")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].day").value("MONDAY"))
                .andExpect(jsonPath("$[0].lockedApps[0]").value("YouTube"));
    }

    @Test
    @DisplayName("앱 잠금 상태 확인")
    void testAppLockStatus() throws Exception {
        mockMvc.perform(get("/detox/time/lock-status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비상 탈출 문장 생성 및 검증")
    void testEscapePhrase() throws Exception {
        MvcResult result = mockMvc.perform(get("/detox/time/generate-phrase"))
                .andExpect(status().isOk())
                .andReturn();

        String phrase = result.getResponse().getContentAsString().trim();

        mockMvc.perform(post("/detox/time/verify-phrase")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(phrase))
                .andExpect(status().isOk());

        mockMvc.perform(post("/detox/time/verify-phrase")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("틀린 문장"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("디톡스 일정 단일 조회")
    void testGetSingleSchedule() throws Exception {
        mockMvc.perform(get("/detox/time/" + scheduleId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.day").value("MONDAY"));
    }

    @Test
    @DisplayName("디톡스 일정 수정")
    void testUpdateSchedule() throws Exception {
        String updateJson = """
            {
                "cycle": "BIWEEKLY",
                "day": "TUESDAY",
                "startTime": "09:00:00",
                "endTime": "17:00:00",
                "lockedApps": ["Twitter", "Netflix"]
            }
        """;

        mockMvc.perform(put("/detox/time/" + scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.day").value("TUESDAY"));
    }

    @Test
    @DisplayName("디톡스 일정 활성화 상태 전환")
    void testToggleActivation() throws Exception {
        mockMvc.perform(patch("/detox/time/" + scheduleId + "/toggle-activation"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("디톡스 일정 삭제")
    void testDeleteSchedule() throws Exception {
        mockMvc.perform(delete("/detox/time/" + scheduleId))
                .andExpect(status().isNoContent());
    }
}
