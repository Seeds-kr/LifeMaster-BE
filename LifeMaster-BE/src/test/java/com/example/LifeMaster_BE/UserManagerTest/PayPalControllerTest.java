package com.example.LifeMaster_BE.UserManagerTest;

import com.example.LifeMaster_BE.Payment.PayPal.PayPalOrderEntity;
import com.example.LifeMaster_BE.Payment.PayPal.PayPalOrderRepository;
import com.example.LifeMaster_BE.Payment.PayPal.PayPalService;
import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
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
import java.util.Optional;

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
    @MockBean private PayPalOrderRepository payPalOrderRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
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
        when(payPalService.createOrder(any(MemberEntity.class)))
                .thenReturn(Map.of(
                        "orderId", "MOCK_ORDER_ID",
                        "approveUrl", "https://paypal.com/approve/MOCK_ORDER_ID"
                ));

        mockMvc.perform(post("/payments/paypal/create-order")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("MOCK_ORDER_ID"))
                .andExpect(jsonPath("$.approveUrl").value("https://paypal.com/approve/MOCK_ORDER_ID"));
    }

    @Test
    @DisplayName("PayPal 주문 수동 캡처 (Mock)")
    void testCaptureOrder() throws Exception {
        String mockOrderId = "MOCK_ORDER_ID";

        when(payPalService.captureOrder(eq(mockOrderId), any(MemberEntity.class)))
                .thenReturn("결제가 성공적으로 완료되었습니다: MOCK_PURCHASE_TOKEN");

        mockMvc.perform(post("/payments/paypal/capture/{orderId}", mockOrderId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("결제가 성공적으로 완료되었습니다: MOCK_PURCHASE_TOKEN"));
    }

    @Test
    @DisplayName("PayPal 결제 성공 콜백 (Mock)")
    void testSuccessCallback() throws Exception {
        String mockOrderId = "MOCK_ORDER_ID";
        String mockPayerId = "MOCK_PAYER_ID";

        MemberEntity member = new MemberEntity();
        member.setId(1L);

        PayPalOrderEntity orderEntity = new PayPalOrderEntity();
        orderEntity.setId(1L);
        orderEntity.setPaypalOrderId(mockOrderId);
        orderEntity.setMember(member);
        orderEntity.setStatus("CREATED");
        orderEntity.setCreatedAt(LocalDateTime.now());

        when(payPalOrderRepository.findByPaypalOrderId(mockOrderId))
                .thenReturn(Optional.of(orderEntity));

        when(payPalService.captureOrder(eq(mockOrderId), any(MemberEntity.class)))
                .thenReturn("결제가 성공적으로 완료되었습니다: MOCK_PURCHASE_TOKEN");

        mockMvc.perform(get("/payments/paypal/success")
                        .param("token", mockOrderId)
                        .param("PayerID", mockPayerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결제 성공 및 저장 완료"))
                .andExpect(jsonPath("$.orderId").value(mockOrderId))
                .andExpect(jsonPath("$.payerId").value(mockPayerId))
                .andExpect(jsonPath("$.result").value("결제가 성공적으로 완료되었습니다: MOCK_PURCHASE_TOKEN"));
    }

    @Test
    @DisplayName("PayPal 결제 취소 콜백 (Mock)")
    void testCancelCallback() throws Exception {
        String mockOrderId = "MOCK_ORDER_ID";

        MemberEntity member = new MemberEntity();
        member.setId(1L);

        PayPalOrderEntity orderEntity = new PayPalOrderEntity();
        orderEntity.setId(1L);
        orderEntity.setPaypalOrderId(mockOrderId);
        orderEntity.setMember(member);
        orderEntity.setStatus("CREATED");
        orderEntity.setCreatedAt(LocalDateTime.now());

        when(payPalOrderRepository.findByPaypalOrderId(mockOrderId))
                .thenReturn(Optional.of(orderEntity));

        mockMvc.perform(get("/payments/paypal/cancel")
                        .param("token", mockOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결제가 취소되었습니다."))
                .andExpect(jsonPath("$.orderId").value(mockOrderId))
                .andExpect(jsonPath("$.cancelledAt").exists());
    }

    @Test
    @DisplayName("내 결제 내역 조회 (Mock)")
    void testGetMyPurchases() throws Exception {
        PurchaseEntity mockPurchase = new PurchaseEntity();
        mockPurchase.setId(1L);
        mockPurchase.setOrderId("MOCK_ORDER_ID");
        mockPurchase.setPurchaseToken("MOCK_PURCHASE_TOKEN");
        mockPurchase.setPackageName("PAYPAL");
        mockPurchase.setPurchaseType("PAYPAL");
        mockPurchase.setSubscriptionId("DEFAULT");
        mockPurchase.setPurchaseState("COMPLETED");
        mockPurchase.setDeveloperPayload("Paid 10.00 USD");
        mockPurchase.setPurchaseTime(LocalDateTime.now());

        when(payPalService.getPurchasesByMember(any(MemberEntity.class)))
                .thenReturn(List.of(mockPurchase));

        mockMvc.perform(get("/payments/paypal/purchaseLog")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("MOCK_ORDER_ID"))
                .andExpect(jsonPath("$[0].purchaseToken").value("MOCK_PURCHASE_TOKEN"));
    }
}