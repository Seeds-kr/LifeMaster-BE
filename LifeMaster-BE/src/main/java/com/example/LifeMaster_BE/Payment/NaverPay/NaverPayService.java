package com.example.LifeMaster_BE.Payment.NaverPay;

import com.example.LifeMaster_BE.Payment.PaymentRequest;
import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Payment.PurchaseRepo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

@Service
public class NaverPayService {

   /* @Value("${naverpay.client-id}")
    private String clientId;

    @Value("${naverpay.client-secret}")
    private String clientSecret;

    @Value("${naverpay.api-url}")
    private String apiUrl;*/

    private String clientId = "clientId";

    private String clientSecret = "clientSecret";

    private String apiUrl = "apiUrl";

    @Autowired
    private PurchaseRepo purchaseRepository;

    // 결제 요청 처리
    @Transactional
    public String createPayment(PaymentRequest paymentRequest) throws Exception {
        // 결제 요청 URL 설정
        String requestUrl = apiUrl + "/payments/request";
        HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();

        // 요청 메소드와 헤더 설정
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Naver-Client-Id", clientId);
        conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // JSON 요청 본문 생성
        String body = String.format(
                "{\"orderId\":\"%s\",\"productName\":\"%s\",\"amount\":%d,\"callbackUrl\":\"http://localhost:8080/naverpay/complete\"}",
                paymentRequest.getOrderId(), paymentRequest.getProductName(), paymentRequest.getAmount()
        );

        // 요청 본문을 OutputStream으로 전송
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        // 서버로부터 응답 읽기
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = br.readLine();

        // 응답에서 paymentId 추출
        JSONObject jsonResponse = new JSONObject(response);

        if (jsonResponse.has("paymentId")) {
            return jsonResponse.getString("paymentId");  // paymentId 반환
        } else {
            throw new Exception("결제 생성에 실패했습니다: " + jsonResponse.toString());
        }
    }

    // 2. 결제 완료
    @Transactional
    public void completePayment(String orderId, String purchaseToken) {
        try {
            // 1. 네이버페이 결제 검증
            boolean isValid = verifyReceipt(purchaseToken, orderId);

            // 2. 검증 성공 시 DB 저장
            if (isValid) {
                PurchaseEntity purchase = new PurchaseEntity();
                purchase.setOrderId(orderId);
                purchase.setPurchaseToken(purchaseToken);
                purchase.setPurchaseState("COMPLETED");
                purchase.setPurchaseTime(LocalDateTime.now());

                purchaseRepository.save(purchase);
                System.out.println("✅ 결제 성공: " + orderId);
            } else {
                System.out.println("❌ 결제 실패: " + orderId);
            }
        } catch (Exception e) {
            System.out.println("❌ 결제 검증 오류: " + e.getMessage());
        }
    }
    // 3. 결제 조회
    public PurchaseEntity getPurchaseInfo(String purchaseToken) {
        return purchaseRepository.findById(purchaseToken)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 결제 토큰입니다."));
    }

    /**
     * 결제 영수증 검증
     * @param purchaseToken 결제 토큰
     * @param orderId 주문 ID
     * @return 결제 상태
     */
    public boolean verifyReceipt(String purchaseToken, String orderId) throws Exception {
        String requestUrl = apiUrl + "/payments/verify";
        HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Naver-Client-Id", clientId);
        conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // 요청 본문 (purchaseToken과 orderId 전송)
        String body = String.format(
                "{\"purchaseToken\":\"%s\",\"orderId\":\"%s\"}",
                purchaseToken, orderId
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = br.readLine();

        // JSON 응답 처리
        JSONObject jsonResponse = new JSONObject(response);
        String status = jsonResponse.getString("status");

        // 결제 상태 확인
        if ("COMPLETED".equals(status)) {
            return true;  // 결제 완료
        } else {
            return false; // 결제 실패 또는 취소
        }
    }
}

