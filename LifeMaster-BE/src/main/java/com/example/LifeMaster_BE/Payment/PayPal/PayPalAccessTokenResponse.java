package com.example.LifeMaster_BE.Payment.PayPal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PayPalAccessTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
