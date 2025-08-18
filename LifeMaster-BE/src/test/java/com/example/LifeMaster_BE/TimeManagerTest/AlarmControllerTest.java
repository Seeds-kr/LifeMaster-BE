package com.example.LifeMaster_BE.TimeManagerTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * AlarmController 통합 테스트
 * - 기존 Group/TimeDetox 테스트와 동일한 스타일
 * - 회원가입/로그인으로 JWT 발급 후 Authorization 헤더 사용
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AlarmControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // --- 유틸: 회원가입/로그인 후 JWT 발급 ---
    private String registerAndLogin(String email) throws Exception {
        // 회원가입
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "%s",
                              "password": "Test1234!",
                              "passwordConfirm": "Test1234!"
                            }
                            """.formatted(email)))
                .andExpect(status().isOk());

        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "%s",
                              "password": "Test1234!"
                            }
                            """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        return loginResult.getResponse().getContentAsString().replace("\"", "");
    }

    // --- 유틸: 알람 생성 후 ID 반환 ---
    private long createAlarmAndGetId(String jwt) throws Exception {
        // NewAlarmDto에 맞는 대표 필드들로 구성
        // (AlarmEntity 스키마에서 확인된 컬럼명을 기준으로 작성)
        String alarmTime = java.time.LocalDate.now().atTime(7, 30).toString(); // "2025-08-18T07:30"
        String newAlarmJson = """
        {
          "alarmTitle": "Morning Call",
          "alarmTime": "%s",
          "alarmSound": "BELL",
          "alarmMon": true,
          "alarmTue": false,
          "alarmWed": false,
          "alarmThu": false,
          "alarmFri": false,
          "alarmSat": false,
          "alarmSun": false,
          "alarmStatus": true
        }
        """.formatted(alarmTime);

        MvcResult res = mockMvc.perform(post("/time/alarm")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAlarmJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.alarmTitle").value("Morning Call"))
                .andExpect(jsonPath("$.alarmTime").exists())
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    @Test
    @DisplayName("알람 전체 흐름: 생성 → 조회 → 요일 상태 업데이트 → 시간차 → 활성화/매칭/비활성화 → 삭제")
    void alarm_full_flow() throws Exception {
        String jwt = registerAndLogin("alarm_owner@example.com");

        // 1) 전체 조회(초기)
        mockMvc.perform(get("/time/alarm"))
                .andExpect(status().isOk());

        // 2) 생성
        long alarmId = createAlarmAndGetId(jwt);

        // 3) 단건 조회 (소유자 컨텍스트)
        mockMvc.perform(get("/time/alarm/{alarmId}", alarmId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alarmId))
                .andExpect(jsonPath("$.alarmTitle").value("Morning Call"));

        // 4) 요일 상태 업데이트 (예: MON false로 변경)
        mockMvc.perform(put("/time/alarm/{alarmId}/update-status", alarmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "day": "MON",
                              "status": false
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alarmId));
        // 세부 필드까지 검증하려면 아래와 같이 확장 가능:
        // .andExpect(jsonPath("$.alarmMon").value(false))

        // 5) 시간 차이 조회
        mockMvc.perform(get("/time/alarm/{alarmId}/time-difference", alarmId))
                .andExpect(status().isOk());

        // 6) 특정 알람 활성화
        mockMvc.perform(put("/time/alarm/{alarmId}/activate", alarmId))
                .andExpect(status().isOk());

        // 7) 현재 시간 기준 매칭되는 알람 활성화
        mockMvc.perform(put("/time/alarm/activate"))
                .andExpect(status().isOk());

        // 8) 활성 알람 비활성화
        mockMvc.perform(post("/time/alarm/deactivate"))
                .andExpect(status().isOk());

        // 9) 삭제
        mockMvc.perform(delete("/time/alarm/{alarmId}", alarmId))
                .andExpect(status().isOk())
                .andExpect(content().string(("Alarm with ID " + alarmId + " has been deleted successfully.")));
    }

    @Test
    @DisplayName("알람 여러 개 생성 후 전체 조회")
    void list_multiple_alarms() throws Exception {
        String jwt = registerAndLogin("list_owner@example.com");

        createAlarmAndGetId(jwt);
        createAlarmAndGetId(jwt);

        mockMvc.perform(get("/time/alarm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

