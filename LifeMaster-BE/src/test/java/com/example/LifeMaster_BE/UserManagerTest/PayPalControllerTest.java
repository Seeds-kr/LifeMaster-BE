package com.example.LifeMaster_BE.UserManagerTest;

import com.example.LifeMaster_BE.Payment.PayPal.PayPalService;
import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PayPalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PayPalService payPalService;

    private String jwtToken;

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
    }

    @Test
    @DisplayName("PayPal 주문 생성 (Mock)")
    void testCreateOrder() throws Exception {
        when(payPalService.createOrder())
                .thenReturn(Map.of(
                        "orderId", "MOCK_ORDER_ID",
                        "approveUrl", "https://paypal.com/approve/MOCK_ORDER_ID"
                ));

        mockMvc.perform(post("/api/paypal/create-order")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("MOCK_ORDER_ID"))
                .andExpect(jsonPath("$.approveUrl").value("https://paypal.com/approve/MOCK_ORDER_ID"));
    }

    @Test
    @DisplayName("PayPal 주문 캡처 (Mock)")
    void testCaptureOrder() throws Exception {
        String mockOrderId = "MOCK_ORDER_ID";

        when(payPalService.captureOrder(eq(mockOrderId), any()))
                .thenReturn("결제가 성공적으로 완료되었습니다: MOCK_PURCHASE_TOKEN");

        mockMvc.perform(post("/api/paypal/capture/" + mockOrderId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("결제가 성공적으로 완료되었습니다: MOCK_PURCHASE_TOKEN"));
    }

    @Test
    @DisplayName("내 결제 내역 조회 (Mock)")
    void testGetMyPurchases() throws Exception {
        // Mock된 구매 내역 하나 생성
        PurchaseEntity mockPurchase = new PurchaseEntity();
        mockPurchase.setId(1L);
        mockPurchase.setOrderId("MOCK_ORDER_ID");
        mockPurchase.setPurchaseToken("MOCK_PURCHASE_TOKEN");
        mockPurchase.setPackageName("paypal");
        mockPurchase.setPurchaseType("paypal");
        mockPurchase.setSubscriptionId("default-subscription");
        mockPurchase.setPurchaseState("COMPLETED");
        mockPurchase.setDeveloperPayload("Paid 10.00 USD");
        mockPurchase.setPurchaseTime(LocalDateTime.now());

        // 구매 목록을 Mock 처리
        when(payPalService.getPurchasesByMember(any()))
                .thenReturn(List.of(mockPurchase));

        mockMvc.perform(get("/api/paypal/purchaseLog")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("MOCK_ORDER_ID"))
                .andExpect(jsonPath("$[0].purchaseToken").value("MOCK_PURCHASE_TOKEN"));
    }
}
