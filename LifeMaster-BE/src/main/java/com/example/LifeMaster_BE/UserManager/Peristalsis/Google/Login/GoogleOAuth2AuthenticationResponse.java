package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;

import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2AccessToken;
import lombok.Getter;

@Getter
public class GoogleOAuth2AuthenticationResponse {
    private final GoogleUsersEntity user;
    private final CustomOAuth2AccessToken token; // OAuth2AccessToken 타입으로 수정

    public GoogleOAuth2AuthenticationResponse(GoogleUsersEntity user, CustomOAuth2AccessToken token) {
        this.user = user;
        this.token = token;
    }

}
