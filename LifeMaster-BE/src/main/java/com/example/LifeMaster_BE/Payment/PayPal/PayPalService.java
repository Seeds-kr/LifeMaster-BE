package com.example.LifeMaster_BE.Payment.PayPal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class PayPalService {

    private final PayPalConfig payPalConfig;
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

    public String createOrder() {
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
        return response.getBody().get("id").toString(); // Order ID
    }

    public String captureOrder(String orderId) {
        String url = payPalConfig.getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        return response.getBody().toString(); // 캡처 결과 반환
    }
}




