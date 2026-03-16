package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;

import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2AccessToken;
import com.example.LifeMaster_BE.UserManager.Peristalsis.OAuthUsersEntity;
import lombok.Getter;

@Getter
public class GoogleOAuth2AuthenticationResponse {
    private final OAuthUsersEntity user;
    private final CustomOAuth2AccessToken token; // OAuth2AccessToken 타입으로 수정
    private final String jwtToken;

    public GoogleOAuth2AuthenticationResponse(OAuthUsersEntity user, CustomOAuth2AccessToken token, String jwtToken) {
        this.user = user;
        this.token = token;
        this.jwtToken = jwtToken;
    }

}
