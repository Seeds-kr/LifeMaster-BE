package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;
/*
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GoogleLoginService extends DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final GoogleRepo googleRepo;

    public GoogleLoginService(GoogleRepo googleRepo) {
        this.googleRepo = googleRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);  // 기본 OAuth2User 가져오기

        // 구글에서 제공하는 사용자 정보
        String googleId = oAuth2User.getAttribute("sub");  // 구글 사용자 ID
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String pictureUrl = oAuth2User.getAttribute("picture");

        Optional<GoogleUserEntity> userOptional = googleRepo.findByGoogleId(googleId);

        if (userOptional.isPresent()) {
            // 기존 사용자 조회
            return new GoogleUserEntity(googleId, email, name, pictureUrl);
        } else {
            // 신규 사용자 등록
            GoogleUserEntity newUser = new GoogleUserEntity(googleId, email, name, pictureUrl);
            googleRepo.save(newUser);
            return new GoogleUserEntity(googleId, email, name, pictureUrl);
        }
    }
}


 */
