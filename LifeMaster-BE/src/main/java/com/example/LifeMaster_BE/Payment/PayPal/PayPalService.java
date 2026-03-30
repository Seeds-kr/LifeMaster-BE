package com.example.LifeMaster_BE.Payment.PayPal;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Payment.PurchaseRepo;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * PayPal 통합 결제 서비스
 * - 주문 생성(createOrder)
 * - 결제 캡처(captureOrder) 및 DB 저장 (공통 스키마: provider/member/productName/amount/currency/...)
 * - 회원별 결제내역 조회(getPurchasesByMember)
 *
 * 전제:
 * 1) PurchaseEntity는 provider/member/amount/currency 등을 포함한 공통 스키마
 * 2) PurchaseRepo에는 existsByPurchaseToken, findAllByMember_IdOrderByPurchaseTimeDesc 존재
 */
@Service
@RequiredArgsConstructor
public class PayPalService {

    private static final String PROVIDER = "PAYPAL";
    private static final String DEFAULT_SUBSCRIPTION = "DEFAULT";
    private static final String COMPLETED = "COMPLETED";

    private final PayPalConfig payPalConfig;
    private final PurchaseRepo purchaseRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String DEFAULT_PRODUCT_NAME = "LifeMaster Premium";
    private static final String RETURN_URL = "http://localhost:8080/paypal/success";
    private static final String CANCEL_URL = "http://localhost:8080/paypal/cancel";

    /**
     * OAuth 토큰 발급
     */
    public String getAccessToken() {
        final String url = payPalConfig.getBaseUrl() + "/v1/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(payPalConfig.getClientId(), payPalConfig.getSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<PayPalAccessTokenResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, request, PayPalAccessTokenResponse.class);

        PayPalAccessTokenResponse body = Objects.requireNonNull(response.getBody(), "Token response is null");
        return Objects.requireNonNull(body.getAccessToken(), "Access token is null");
    }

    /**
     * 결제 주문 생성
     * - 현재는 하드코딩(USD 10.00). 필요 시 파라미터로 금액/상품명/통화 받도록 확장
     */
    public Map<String, String> createOrder() {
        final String url = payPalConfig.getBaseUrl() + "/v2/checkout/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> amount = new HashMap<>();
        amount.put("currency_code", "USD");
        amount.put("value", "10.00");

        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("amount", amount);
        purchaseUnit.put("description", DEFAULT_PRODUCT_NAME);

        Map<String, Object> applicationContext = new HashMap<>();
        applicationContext.put("return_url", RETURN_URL);
        applicationContext.put("cancel_url", CANCEL_URL);
        applicationContext.put("shipping_preference", "NO_SHIPPING");
        applicationContext.put("user_action", "PAY_NOW");
        applicationContext.put("brand_name", "LifeMaster");

        Map<String, Object> payload = new HashMap<>();
        payload.put("intent", "CAPTURE");
        payload.put("purchase_units", List.of(purchaseUnit));
        payload.put("application_context", applicationContext);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Invalid PayPal order response: null body");
        }

        Object idObj = responseBody.get("id");
        if (idObj == null || String.valueOf(idObj).isBlank()) {
            throw new RuntimeException("Invalid PayPal order response: missing id");
        }
        String orderId = String.valueOf(idObj);

        String approveUrl = null;
        Object linksObj = responseBody.get("links");
        if (linksObj instanceof List<?> linksList) {
            for (Object linkObj : linksList) {
                if (linkObj instanceof Map<?, ?> link) {
                    Object rel = link.get("rel");
                    Object href = link.get("href");

                    if ("approve".equals(String.valueOf(rel)) && href != null) {
                        approveUrl = String.valueOf(href);
                        break;
                    }
                }
            }
        }

        if (approveUrl == null || approveUrl.isBlank()) {
            throw new RuntimeException("Invalid PayPal order response: missing approve link");
        }

        return Map.of(
                "orderId", orderId,
                "approveUrl", approveUrl
        );
    }

    /**
     * 결제 캡처 & DB 저장(멱등성 보장: purchaseToken 중복 저장 방지)
     */
    public String captureOrder(String orderId, MemberEntity member) {
        final String url = payPalConfig.getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("Invalid PayPal capture response: null body");
        }

        // 전체 상태 확인 (COMPLETED 여야 함)
        String status = String.valueOf(body.get("status"));
        if (!COMPLETED.equalsIgnoreCase(status)) {
            throw new RuntimeException("Payment was not completed. status=" + status);
        }

        // 첫 번째 purchase_unit 취득
        Map<String, Object> purchaseUnit = firstPurchaseUnit(body);
        // 첫 번째 capture 취득
        Map<String, Object> capture = firstCapture(purchaseUnit);

        // 멱등성 체크 (purchaseToken = capture.id)
        String purchaseToken = String.valueOf(capture.get("id"));
        if (purchaseRepository.existsByPurchaseToken(purchaseToken)) {
            return "이미 처리된 결제입니다: " + purchaseToken;
        }

        // 엔티티 구성 및 저장
        PurchaseEntity entity = buildPurchaseEntity(orderId, purchaseUnit, capture, member);
        purchaseRepository.save(entity);

        return "결제가 성공적으로 완료되었습니다: " + entity.getPurchaseToken();
    }

    /**
     * 회원별 결제내역(최신순)
     * - 레포에 findAllByMember_IdOrderByPurchaseTimeDesc가 없다면 findByMember(member)로 교체
     */
    public List<PurchaseEntity> getPurchasesByMember(MemberEntity member) {
        // 권장
        try {
            return purchaseRepository.findAllByMember_IdOrderByPurchaseTimeDesc(member.getId());
        } catch (Exception ignored) {
            return purchaseRepository.findAllByMember_IdOrderByPurchaseTimeDesc(member.getId());
        }
    }

    /* -------------------------- 내부 유틸 -------------------------- */

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstPurchaseUnit(Map<String, Object> body) {
        Object unitsObj = body.get("purchase_units");
        if (!(unitsObj instanceof List<?> unitsList) || unitsList.isEmpty()) {
            throw new RuntimeException("Invalid capture response: missing purchase_units");
        }
        Object first = unitsList.get(0);
        if (!(first instanceof Map<?, ?> unit)) {
            throw new RuntimeException("Invalid capture response: purchase_units[0] not a map");
        }
        return (Map<String, Object>) unit;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstCapture(Map<String, Object> purchaseUnit) {
        Object paymentsObj = purchaseUnit.get("payments");
        if (!(paymentsObj instanceof Map<?, ?> payments)) {
            throw new RuntimeException("Invalid capture response: missing payments");
        }
        Object capturesObj = payments.get("captures");
        if (!(capturesObj instanceof List<?> captures) || captures.isEmpty()) {
            throw new RuntimeException("Invalid capture response: missing captures");
        }
        Object first = captures.get(0);
        if (!(first instanceof Map<?, ?> cap)) {
            throw new RuntimeException("Invalid capture response: captures[0] not a map");
        }
        return (Map<String, Object>) cap;
    }

    /**
     * PayPal → PurchaseEntity 매핑 (공통 스키마 채움)
     */
    private PurchaseEntity buildPurchaseEntity(String orderId,
                                               Map<String, Object> purchaseUnit,
                                               Map<String, Object> capture,
                                               MemberEntity member) {

        String purchaseToken = String.valueOf(capture.get("id"));

        // amount / currency
        @SuppressWarnings("unchecked")
        Map<String, Object> amountMap = (Map<String, Object>) capture.get("amount");
        String amountStr = amountMap != null ? String.valueOf(amountMap.get("value")) : null;
        String currency = amountMap != null ? String.valueOf(amountMap.get("currency_code")) : null;

        // create_time
        LocalDateTime purchaseTime = parseIsoDateTimeOrNow(String.valueOf(capture.get("create_time")));

        // productName (purchase_unit.description 사용)
        String productName = DEFAULT_PRODUCT_NAME;
        if (purchaseUnit != null && purchaseUnit.get("description") != null) {
            productName = String.valueOf(purchaseUnit.get("description"));
        }

        // 금액 정수 변환 (필요 시 BigDecimal을 Entity에 사용 권장)
        Integer amountInt = null;
        if (amountStr != null && !amountStr.isBlank()) {
            try {
                amountInt = new BigDecimal(amountStr).setScale(0, BigDecimal.ROUND_HALF_UP).intValueExact();
            } catch (Exception ignored) { /* null 허용 */ }
        }

        PurchaseEntity e = new PurchaseEntity();
        e.setPurchaseToken(purchaseToken);

        // 공통 스키마 채우기
        e.setProvider(PROVIDER);                // "PAYPAL"
        e.setMember(member);                    // 회원
        e.setProductName(productName);
        e.setAmount(amountInt);
        e.setCurrency(currency);

        // 참고용(플랫폼 특화 필드)
        e.setOrderId(orderId);
        e.setPackageName(PROVIDER);             // 플랫폼명을 packageName에 기록(선택)
        e.setSubscriptionId(DEFAULT_SUBSCRIPTION);
        e.setPurchaseType(PROVIDER);
        e.setPurchaseState(COMPLETED);
        e.setPurchaseTime(purchaseTime);
        e.setDeveloperPayload("Paid " + amountStr + " " + (currency != null ? currency : ""));

        return e;
    }

    private LocalDateTime parseIsoDateTimeOrNow(String iso) {
        if (iso == null || iso.isBlank()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(iso, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
