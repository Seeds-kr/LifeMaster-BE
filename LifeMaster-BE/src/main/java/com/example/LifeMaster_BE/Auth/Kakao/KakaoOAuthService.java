package com.example.LifeMaster_BE.Auth.Kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KakaoOAuthService {

    private final String clientId;
    private final String redirectUri;

    // 생성자에서 @Value 어노테이션으로 값 주입
    public KakaoOAuthService(
            @Value("${jwt.kakao.secretKey}") String clientId,
            @Value("${jwt.kakao.redirectUri}") String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    // 카카오 액세스 토큰을 가져오는 메서드
    public String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&code=" + code;

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            return (String) responseBody.get("access_token");
        }
        throw new RuntimeException("Failed to get Kakao Access Token");
    }

    // 카카오 사용자 정보를 가져오는 메서드
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // Bearer 토큰 헤더 추가

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        throw new RuntimeException("Failed to get user info from Kakao");
    }
}

