package com.example.LifeMaster_BE;

import com.example.LifeMaster_BE.FunctionManager.Calender.EventRequestDto;
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

import java.util.List;
import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ScheduleCalendarControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String date;

    @BeforeEach
    void setUp() throws Exception {
        // 랜덤 날짜 생성 (중복 방지)
        date = "202507" + (10 + new Random().nextInt(20));

        mockMvc.perform(post("/calendar/create")
                        .param("date", date))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("전체 조회")
    void testGetAllEntries() throws Exception {
        mockMvc.perform(get("/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value(date));
    }

    @Test
    @DisplayName("특정 날짜 조회")
    void testGetEntryByDate() throws Exception {
        mockMvc.perform(get("/calendar/" + date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date));
    }

    @Test
    @DisplayName("월별 조회")
    void testGetEntriesByMonth() throws Exception {
        mockMvc.perform(get("/calendar/month/" + date.substring(0, 6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value(date));
    }

    @Test
    @DisplayName("특정 날짜에 이벤트 추가")
    void testAddEvent() throws Exception {
        EventRequestDto request = new EventRequestDto();
        request.setEvent("회의 일정");

        mockMvc.perform(post("/calendar/" + date + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0]").value("회의 일정")); // ✔️ DTO 기반 비교
    }

    @Test
    @DisplayName("새로운 날짜에 이벤트 리스트 생성")
    void testCreateEventList() throws Exception {
        List<String> events = List.of("출근", "회의", "운동");
        String newDate = "20250728";

        mockMvc.perform(post("/calendar/create/" + newDate + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(events)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(3))
                .andExpect(jsonPath("$.date").value(newDate));
    }

    @Test
    @DisplayName("특정 날짜의 특정 이벤트 삭제")
    void testDeleteSpecificEvent() throws Exception {
        String event = "삭제 테스트";

        // DTO 객체 생성
        EventRequestDto request = new EventRequestDto();
        request.setEvent(event);

        // 먼저 이벤트 추가
        mockMvc.perform(post("/calendar/" + date + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 이벤트 삭제
        mockMvc.perform(delete("/calendar/" + date + "/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(0));
    }

    @Test
    @DisplayName("특정 날짜의 모든 이벤트 삭제")
    void testDeleteAllEventsByDate() throws Exception {
        mockMvc.perform(post("/calendar/" + date + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("이벤트1")));

        mockMvc.perform(post("/calendar/" + date + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("이벤트2")));

        mockMvc.perform(delete("/calendar/" + date))
                .andExpect(status().isOk())
                .andExpect(content().string("삭제되었습니다."));

        mockMvc.perform(get("/calendar/" + date))
                .andExpect(status().isNotFound());
    }
}