package com.example.LifeMaster_BE.Payment.GooglePlay;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Payment.PurchaseRepo;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class GooglePlayService {
    private String packageName = "aaaa";

    private String credentialsFile = "asdfasdf";

    private final ResourceLoader resourceLoader;
    private final PurchaseRepo purchaseRepository;

    public GooglePlayService(ResourceLoader resourceLoader, PurchaseRepo purchaseRepository) {
        this.resourceLoader = resourceLoader;
        this.purchaseRepository = purchaseRepository;
    }

    private AndroidPublisher initGooglePlayClient() throws IOException {
        Resource resource = resourceLoader.getResource(credentialsFile);
        try (InputStream inputStream = resource.getInputStream()) {
            // Load credentials from the service account file
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream)
                    .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            // Set up the HttpRequestInitializer to apply the credentials
            HttpRequestInitializer requestInitializer = new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    credentials.refreshIfExpired();  // Ensure the credentials are valid
                    credentials.getAccessToken();  // Retrieve the current access token
                    request.getHeaders().setAuthorization("Bearer " + credentials.getAccessToken().getTokenValue());
                }
            };

            return new AndroidPublisher.Builder(httpTransport, jsonFactory, requestInitializer)
                    .setApplicationName("Your App Name")
                    .build();
        }
    }

    // 구독 구매 확인 및 DB 저장
    public SubscriptionPurchase verifyAndSavePurchase(String subscriptionId, String purchaseToken) {
        try {
            AndroidPublisher publisher = initGooglePlayClient();
            SubscriptionPurchase purchase = publisher.purchases().subscriptions()
                    .get(packageName, subscriptionId, purchaseToken)
                    .execute();

            // 결제 내역을 DB에 저장
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setPurchaseToken(purchaseToken);
            purchaseEntity.setSubscriptionId(subscriptionId);
            purchaseEntity.setPackageName(packageName);
            purchaseEntity.setPurchaseState(purchase.getPaymentState().toString());
            purchaseEntity.setPurchaseTime(LocalDateTime.now());  // 실제 구매 시간으로 변경 가능
            purchaseEntity.setPurchaseType(purchase.getPurchaseType().toString());
            purchaseEntity.setOrderId(purchase.getOrderId());
            purchaseEntity.setDeveloperPayload(purchase.getDeveloperPayload());

            purchaseRepository.save(purchaseEntity);

            return purchase;
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify and save purchase: " + e.getMessage(), e);
        }
    }
}
