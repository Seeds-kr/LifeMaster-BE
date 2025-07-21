package com.example.LifeMaster_BE;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MemberSubscriptionIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;

    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        // 회원가입
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "subtest@example.com",
                                  "password": "Test1234!",
                                  "passwordConfirm": "Test1234!"
                                }
                                """))
                .andExpect(status().isOk());

        // 로그인 (JWT 토큰 필요 시 사용할 수 있도록 유지)
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "subtest@example.com",
                                  "password": "Test1234!"
                                }
                                """))
                .andExpect(status().isOk());

        // 리포지토리에서 사용자 ID 조회
        MemberEntity member = memberRepository.findByEmail("subtest@example.com")
                .orElseThrow(() -> new RuntimeException("회원 조회 실패"));

        userId = member.getId();
    }

    @Test
    @DisplayName("구독 정보 기본 조회")
    void testGetSubscriptionInfo() throws Exception {
        mockMvc.perform(get("/user/" + userId + "/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionPlan").value("FREE"))
                .andExpect(jsonPath("$.paymentStatus").value("UNPAID"));
    }

    @Test
    @DisplayName("구독 플랜 변경")
    void testUpdateSubscription() throws Exception {
        mockMvc.perform(put("/user/" + userId + "/subscription")
                        .param("plan", "PREMIUM")) // 사용 가능한 Enum 값
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionPlan").value("PREMIUM"));
    }

    @Test
    @DisplayName("결제 내역 추가")
    void testAddPayment() throws Exception {
        mockMvc.perform(post("/user/" + userId + "/payment")
                        .param("amount", "19.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(19.99));
    }

    @Test
    @DisplayName("결제 내역 조회")
    void testGetPaymentHistory() throws Exception {
        mockMvc.perform(post("/user/" + userId + "/payment")
                        .param("amount", "19.99"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user/" + userId + "/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(19.99));
    }
}
