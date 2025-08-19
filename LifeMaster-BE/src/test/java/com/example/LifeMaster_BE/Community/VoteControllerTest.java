package com.example.LifeMaster_BE.Community;

import com.example.LifeMaster_BE.Community.Vote.VoteDTO;
import com.example.LifeMaster_BE.Community.Vote.VoteEntity;
import com.example.LifeMaster_BE.Community.Vote.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
class VoteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean
    VoteService voteService;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // 회원가입
        mockMvc.perform(post("/user/register")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "email": "testuser@example.com",
                      "password": "Test1234!",
                      "passwordConfirm": "Test1234!"
                    }
                """)).andExpect(status().isOk());

        // 로그인 → 토큰 획득
        MvcResult result = mockMvc.perform(post("/user/login")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "email": "testuser@example.com",
                      "password": "Test1234!"
                    }
                """)).andExpect(status().isOk()).andReturn();

        jwtToken = result.getResponse().getContentAsString().replace("\"", "");
    }

    @Test
    @DisplayName("투표 생성 성공")
    void createPoll_success() throws Exception {
        VoteDTO.PollRequest request = new VoteDTO.PollRequest();
        request.setTitle("개선안 투표");
        request.setEndDate(LocalDateTime.parse("2025-12-31T00:00:00"));
        request.setOptions(List.of("옵션1", "옵션2"));

        mockMvc.perform(post("/community/improvePost/poll")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("투표 항목 투표 요청")
    void castVote_success() throws Exception {
        VoteDTO.VoteRequest request = new VoteDTO.VoteRequest();
        request.setOptionId(1L);
        request.setUserId("2");

        mockMvc.perform(post("/community/improvePost/poll/10/vote")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("투표 결과 조회")
    void getPollResults() throws Exception {
        mockMvc.perform(get("/community/improvePost/poll/10/results")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("투표 상세 조회")
    void getPollDetails() throws Exception {
        mockMvc.perform(get("/community/improvePost/poll/11/details")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("투표 제목 수정")
    void updatePollTitle() throws Exception {
        VoteDTO.PollTitleRequest request = new VoteDTO.PollTitleRequest();
        request.setTitle("새 제목");

        mockMvc.perform(put("/community/improvePost/poll/12/title")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("투표 항목 추가")
    void addPollOption() throws Exception {
        // 요청 객체 설정
        VoteDTO.PollOptionRequest request = new VoteDTO.PollOptionRequest();
        request.setContent("추가 항목");

        // 반환될 PollOption 객체 설정 (모킹)
        VoteEntity.PollOption mockedOption = new VoteEntity.PollOption();
        mockedOption.setId(99L);  // ID는 없어도 됨
        mockedOption.setContent("추가 항목");

        // voteService 모킹 설정
        Mockito.when(voteService.addPollOption(eq(13L), eq("추가 항목")))
                .thenReturn(mockedOption);

        // 요청 및 검증
        mockMvc.perform(post("/community/improvePost/poll/13/options")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("추가 항목"));
    }
    @Test
    @DisplayName("투표 삭제")
    void deletePoll() throws Exception {
        mockMvc.perform(delete("/community/improvePost/poll/14")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("모든 투표 목록 확인")
    void getAllPolls() throws Exception {
        mockMvc.perform(get("/community/improvePost/poll/all")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }
}
