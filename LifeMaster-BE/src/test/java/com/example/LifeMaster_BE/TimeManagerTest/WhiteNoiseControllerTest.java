package com.example.LifeMaster_BE.TimeManagerTest;

import com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise.MusicCategory;
import com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise.WhiteNoiseDTO;
import com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise.WhiteNoiseResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WhiteNoiseControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String jwtToken;
    private Long whiteNoiseId;

    @BeforeEach
    void setUp() throws Exception {
        // 회원가입
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email": "testuser@example.com",
                      "password": "Test1234!",
                      "passwordConfirm": "Test1234!"
                    }
                """))
                .andExpect(status().isOk());

        // 로그인
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email": "testuser@example.com",
                      "password": "Test1234!"
                    }
                """))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = result.getResponse().getContentAsString().replace("\"", "");

        // 백색소음 생성
        WhiteNoiseDTO dto = new WhiteNoiseDTO();
        dto.setTitle("테스트 소리");
        dto.setDescription("설명입니다");
        dto.setThumbnailUrl("https://cdn.com/image.jpg");
        dto.setAudioUri("https://cdn.com/audio.mp3");
        dto.setCategory(MusicCategory.WHITE_NOISE);

        MvcResult createResult = mockMvc.perform(post("/time/sleep/playlist")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        // ✅ 응답을 안전하게 역직렬화
        WhiteNoiseResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                WhiteNoiseResponse.class
        );
        whiteNoiseId = created.getId();
    }

    @Test
    @DisplayName("전체 백색소음 조회")
    void testGetAllWhiteNoises() throws Exception {
        mockMvc.perform(get("/time/sleep/playlist")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("ID로 백색소음 조회")
    void testGetWhiteNoiseById() throws Exception {
        mockMvc.perform(get("/time/sleep/playlist/" + whiteNoiseId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 소리"));
    }

    @Test
    @DisplayName("백색소음 수정")
    void testUpdateWhiteNoise() throws Exception {
        WhiteNoiseDTO updateDTO = new WhiteNoiseDTO();
        updateDTO.setTitle("수정된 소리");
        updateDTO.setDescription("업데이트된 설명");
        updateDTO.setThumbnailUrl("https://cdn.com/updated.jpg");
        updateDTO.setAudioUri("https://cdn.com/updated.mp3");
        updateDTO.setCategory(MusicCategory.CLASSICAL);

        mockMvc.perform(put("/time/sleep/playlist/" + whiteNoiseId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 소리"))
                .andExpect(jsonPath("$.category").value("클래식"));
    }

    @Test
    @DisplayName("백색소음 삭제")
    void testDeleteWhiteNoise() throws Exception {
        mockMvc.perform(delete("/time/sleep/playlist/" + whiteNoiseId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/time/sleep/playlist")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

