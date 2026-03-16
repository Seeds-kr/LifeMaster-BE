package com.example.LifeMaster_BE.Payment.PayPal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paypal")
@Getter
@Setter
public class PayPalConfig {
    private String clientId;
    private String secret;
    private String baseUrl;
}