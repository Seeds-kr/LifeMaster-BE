package com.example.LifeMaster_BE.Payment.NaverPay;

import com.example.LifeMaster_BE.Payment.PaymentRequest;
import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Payment.PurchaseRepo;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NaverPayService {

    // 실제 운영에서는 @Value 로 주입하세요.
    @Value("${naverpay.client-id}")     private String clientId;
    @Value("${naverpay.client-secret}") private String clientSecret;
    @Value("${naverpay.api-url}")       private String apiUrl;

    private static final String PROVIDER = "NAVERPAY";
    private static final String STATE_COMPLETED = "COMPLETED";
    private static final String DEFAULT_CURRENCY = "KRW";

    private final PurchaseRepo purchaseRepository;

    /**
     * 결제 요청 생성
     */
    @Transactional
    public String createPayment(PaymentRequest paymentRequest) throws Exception {
        String requestUrl = apiUrl + "/payments/request";
        HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Naver-Client-Id", clientId);
        conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // callbackUrl 은 예시
        String body = String.format(
                "{\"orderId\":\"%s\",\"productName\":\"%s\",\"amount\":%d,\"callbackUrl\":\"http://localhost:8080/naverpay/complete\"}",
                paymentRequest.getOrderId(), paymentRequest.getProductName(), paymentRequest.getAmount()
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = br.readLine();
            JSONObject json = new JSONObject(response);
            if (json.has("paymentId")) {
                return json.getString("paymentId");
            }
            throw new Exception("결제 생성 실패: " + json);
        }
    }

    /**
     * ✅ 새 시그니처: 결제 완료 + 저장(공통 스키마 세팅)
     * - member / productName / amount / currency 를 함께 받아 저장
     * - currency 미지정 시 KRW 기본값
     */
    @Transactional
    public void completePayment(String orderId,
                                String purchaseToken,
                                MemberEntity member,
                                String productName,
                                Integer amount,
                                String currency) {
        try {
            boolean isValid = verifyReceipt(purchaseToken, orderId);
            if (!isValid) {
                System.out.println("❌ 결제 실패: " + orderId);
                return;
            }

            // 멱등성: 중복 저장 방지
            if (purchaseRepository.existsByPurchaseToken(purchaseToken)) {
                System.out.println("ℹ️ 이미 처리된 결제: " + purchaseToken);
                return;
            }

            PurchaseEntity e = new PurchaseEntity();
            e.setOrderId(orderId);
            e.setPurchaseToken(purchaseToken);
            e.setPurchaseState(STATE_COMPLETED);
            e.setPurchaseTime(LocalDateTime.now());

            // ✅ 공통 스키마
            e.setProvider(PROVIDER);
            if (member != null) e.setMember(member);
            e.setProductName(productName);
            e.setAmount(amount);
            e.setCurrency((currency == null || currency.isBlank()) ? DEFAULT_CURRENCY : currency);

            purchaseRepository.save(e);
            System.out.println("✅ 결제 성공: " + orderId);

        } catch (Exception e) {
            System.out.println("❌ 결제 검증/저장 오류: " + e.getMessage());
        }
    }

    /**
     * ⚠️ 레거시 호환용: 기존 시그니처 유지
     * - member/product 정보가 없으므로 종합 조회(회원별)에는 잡히지 않을 수 있음
     * - 가능한 위의 새 시그니처 사용 권장
     */
    @Transactional
    public void completePayment(String orderId, String purchaseToken) {
        // 필요한 경우 여기서 member/product 정보를 조회해 채워 넣도록 확장 가능
        completePayment(orderId, purchaseToken, null, null, null, null);
    }

    /**
     * 결제 단건 조회
     */
    @Transactional(readOnly = true)
    public PurchaseEntity getPurchaseInfo(String purchaseToken) {
        return purchaseRepository.findById(purchaseToken)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 결제 토큰입니다."));
    }

    /**
     * 영수증 검증
     */
    public boolean verifyReceipt(String purchaseToken, String orderId) throws Exception {
        String requestUrl = apiUrl + "/payments/verify";
        HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Naver-Client-Id", clientId);
        conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String body = String.format("{\"purchaseToken\":\"%s\",\"orderId\":\"%s\"}", purchaseToken, orderId);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = br.readLine();
            JSONObject json = new JSONObject(response);
            String status = json.optString("status", "");
            return STATE_COMPLETED.equals(status);
        }
    }
}
