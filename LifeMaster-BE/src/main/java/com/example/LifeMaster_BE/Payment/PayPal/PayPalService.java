package com.example.LifeMaster_BE.Payment.PayPal;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Payment.PurchaseRepo;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PayPalService {

    private static final String PAYPAL = "paypal";
    private static final String DEFAULT_SUBSCRIPTION = "default-subscription";
    private static final String COMPLETED = "COMPLETED";

    private final PayPalConfig payPalConfig;
    private final PurchaseRepo purchaseRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public String getAccessToken() {
        String url = payPalConfig.getBaseUrl() + "/v1/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(payPalConfig.getClientId(), payPalConfig.getSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<PayPalAccessTokenResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, request, PayPalAccessTokenResponse.class);

        return Objects.requireNonNull(response.getBody()).getAccessToken();
    }

    public Map<String, String> createOrder() {
        String url = payPalConfig.getBaseUrl() + "/v2/checkout/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> amount = Map.of(
                "currency_code", "USD",
                "value", "10.00"
        );

        Map<String, Object> purchaseUnit = Map.of("amount", amount);

        Map<String, Object> payload = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(purchaseUnit)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("id") || !responseBody.containsKey("links")) {
            throw new RuntimeException("Invalid PayPal order response");
        }

        String orderId = responseBody.get("id").toString();
        String approveUrl = "";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> links = (List<Map<String, Object>>) responseBody.get("links");
        for (Map<String, Object> link : links) {
            if ("approve".equals(link.get("rel"))) {
                approveUrl = link.get("href").toString();
                break;
            }
        }

        return Map.of(
                "orderId", orderId,
                "approveUrl", approveUrl
        );
    }

    public String captureOrder(String orderId, MemberEntity member) {
        String url = payPalConfig.getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !COMPLETED.equalsIgnoreCase(String.valueOf(responseBody.get("status")))) {
            throw new RuntimeException("Payment was not completed");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> purchaseUnits = (List<Map<String, Object>>) responseBody.get("purchase_units");
        Map<String, Object> purchaseUnit = purchaseUnits.get(0);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> captures = (List<Map<String, Object>>)
                ((Map<String, Object>) purchaseUnit.get("payments")).get("captures");

        Map<String, Object> capture = captures.getFirst();

        PurchaseEntity entity = buildPurchaseEntity(orderId, capture,member);
        System.out.println(capture);
        purchaseRepository.save(entity);

        return "결제가 성공적으로 완료되었습니다: " + entity.getPurchaseToken();
    }

    private PurchaseEntity buildPurchaseEntity(String orderId, Map<String, Object> capture,MemberEntity member) {
        String purchaseToken = capture.get("id").toString();
        @SuppressWarnings("unchecked")
        Map<String, Object> amountMap = (Map<String, Object>) capture.get("amount");

        String amount = amountMap.get("value").toString();
        String currency = amountMap.get("currency_code").toString();
        String createTimeStr = capture.get("create_time").toString();

        PurchaseEntity entity = new PurchaseEntity();
        entity.setPurchaseToken(purchaseToken);
        entity.setMember(member);
        entity.setOrderId(orderId);
        entity.setPackageName(PAYPAL);
        entity.setSubscriptionId(DEFAULT_SUBSCRIPTION);
        entity.setPurchaseType(PAYPAL);
        entity.setPurchaseState(COMPLETED);
        entity.setPurchaseTime(LocalDateTime.parse(createTimeStr, DateTimeFormatter.ISO_DATE_TIME));
        entity.setDeveloperPayload("Paid " + amount + " " + currency);

        return entity;
    }

    public List<PurchaseEntity> getPurchasesByMember(MemberEntity member) {
        return purchaseRepository.findByMember(member);
    }
}
