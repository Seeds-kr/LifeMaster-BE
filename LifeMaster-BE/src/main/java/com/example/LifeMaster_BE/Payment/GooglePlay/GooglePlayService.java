package com.example.LifeMaster_BE.Payment.GooglePlay;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Payment.PurchaseRepo;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GooglePlayService {


    @Value("${google.play.package-name:aaaa}")
    private String packageName;

    @Value("${google.play.credentials-file:asdfasdf}")
    private String credentialsFile;

    private static final String PROVIDER = "GOOGLEPLAY";
    private static final String DEFAULT_CURRENCY = "KRW";

    private final ResourceLoader resourceLoader;
    private final PurchaseRepo purchaseRepository;

    private AndroidPublisher initGooglePlayClient() throws IOException {
        Resource resource = resourceLoader.getResource(credentialsFile);
        try (InputStream inputStream = resource.getInputStream()) {
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream)
                    .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            HttpRequestInitializer requestInitializer = new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    credentials.refreshIfExpired();
                    request.getHeaders().setAuthorization("Bearer " + credentials.getAccessToken().getTokenValue());
                }
            };

            return new AndroidPublisher.Builder(httpTransport, jsonFactory, requestInitializer)
                    .setApplicationName("Your App Name")
                    .build();
        }
    }

    /**
     * 구독 구매 검증 및 DB 저장(공통 스키마 세팅)
     * @param subscriptionId Google Play 구독 상품 ID (SKU)
     * @param purchaseToken  클라이언트에서 전달한 purchaseToken
     * @param member         로그인 사용자(회원) — FK 저장용 (ID만 있어도 OK)
     * @param productName    표시용 상품명
     * @param amount         금액(정수; 필요시 BigDecimal로 변경 고려)
     * @param currency       통화코드(기본 KRW)
     */
    @Transactional
    public SubscriptionPurchase verifyAndSavePurchase(String subscriptionId,
                                                      String purchaseToken,
                                                      MemberEntity member,
                                                      String productName,
                                                      Integer amount,
                                                      String currency) {
        try {
            AndroidPublisher publisher = initGooglePlayClient();

            // Google API 호출: 구독 상태 조회
            SubscriptionPurchase purchase = publisher.purchases().subscriptions()
                    .get(packageName, subscriptionId, purchaseToken)
                    .execute();

            // 멱등성: 이미 처리된 토큰이면 스킵
            if (purchaseRepository.existsByPurchaseToken(purchaseToken)) {
                return purchase;
            }

            // 구매시각(가능하면 Google의 startTimeMillis 등을 매핑)
            LocalDateTime purchasedAt = nowFromGoogle(purchase);

            // 공통 스키마 매핑
            PurchaseEntity e = new PurchaseEntity();
            e.setPurchaseToken(purchaseToken);
            e.setSubscriptionId(subscriptionId);
            e.setPackageName(packageName);
            e.setPurchaseState(String.valueOf(purchase.getPaymentState())); // 0: pending, 1: received 등
            e.setPurchaseType(String.valueOf(purchase.getPurchaseType()));  // 구글 정의(Nullable 가능)
            e.setOrderId(purchase.getOrderId());
            e.setDeveloperPayload(purchase.getDeveloperPayload());
            e.setPurchaseTime(purchasedAt);

            // ✅ 공통 필드
            e.setProvider(PROVIDER);
            if (member != null) e.setMember(member);
            e.setProductName(productName);
            e.setAmount(amount);
            e.setCurrency((currency == null || currency.isBlank()) ? DEFAULT_CURRENCY : currency);

            purchaseRepository.save(e);

            return purchase;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to verify and save purchase: " + ex.getMessage(), ex);
        }
    }

    /** Google 응답에서 구매시각 추출(가능한 값 우선 사용, 없으면 now) */
    private LocalDateTime nowFromGoogle(SubscriptionPurchase purchase) {
        try {
            // Auto-renewing subscriptions often provide startTimeMillis or linkedTimeMillis
            Long millis = purchase.getStartTimeMillis();
            if (millis == null) millis = purchase.getLinkedPurchaseToken() != null ? purchase.getStartTimeMillis() : null;
            if (millis != null) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
            }
        } catch (Exception ignored) {}
        return LocalDateTime.now();
    }
}
