package com.example.LifeMaster_BE.GooglePlay;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GooglePlayService {
    @Value("${google.play.package-name}")
    private String packageName;

    @Value("${google.play.credentials-file}")
    private String credentialsFile;

    private final ResourceLoader resourceLoader;

    public GooglePlayService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private AndroidPublisher initGooglePlayClient() throws IOException, GeneralSecurityException {
        Resource resource = resourceLoader.getResource(credentialsFile);
        GoogleCredential credential = GoogleCredential.fromStream(resource.getInputStream())
                .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        return new AndroidPublisher.Builder(
                credential.getTransport(),
                credential.getJsonFactory(),
                credential)
                .setApplicationName("Your App Name")
                .build();
    }

    public SubscriptionPurchase verifyPurchase(String subscriptionId, String purchaseToken) {
        try {
            AndroidPublisher publisher = initGooglePlayClient();
            return publisher.purchases().subscriptions()
                    .get(packageName, subscriptionId, purchaseToken)
                    .execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to verify purchase: " + e.getMessage(), e);
        }
    }
}
