package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
public class GoogleOAuthProperties {

    // Getters and setters
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;

}

