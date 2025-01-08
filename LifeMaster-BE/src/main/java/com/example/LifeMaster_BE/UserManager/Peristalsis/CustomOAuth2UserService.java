package com.example.LifeMaster_BE.UserManager.Peristalsis;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleOAuth2AuthenticationResponse;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleOAuthProperties;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleUsersEntity;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleUsersRepository;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Naver.NaverOAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {//네이버/구글 로그인 기능 관련 서비스

    @Autowired
    private GoogleOAuthProperties googleOAuthProperties;
    @Autowired
    private NaverOAuthProperties naverOAuthProperties;
    private final GoogleUsersRepository googleUsersRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    public CustomOAuth2UserService(GoogleUsersRepository googleUsersRepository, @Lazy AuthenticationManager authenticationManager, JwtUtil jwtUtil, MemberRepository memberRepository) {
        this.googleUsersRepository = googleUsersRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
    }


    public GoogleUsersEntity loadGoogleUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        RestTemplate restTemplate = new RestTemplate();
        // 액세스 토큰 가져오기
        String accessToken = userRequest.getAccessToken().getTokenValue();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // Authorization 헤더에 Bearer 토큰 추가

        HttpEntity<String> entity = new HttpEntity<>(headers);


        ResponseEntity<Map> response = restTemplate.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, entity, Map.class);

        Map<String, Object> userAttributes = response.getBody(); // 사용자 정보

        // Map to GoogleUsers
        String id = (String) userAttributes.get("sub");  // sub is the unique identifier
        String name = (String) userAttributes.get("name");
        String email = (String) userAttributes.get("email");
        String picture = (String) userAttributes.get("picture");

        GoogleUsersEntity googleUser;
        Optional<GoogleUsersEntity> optionalUser = googleUsersRepository.findByIdentifier(id);

        if (optionalUser.isPresent()) {
            // 기존 유저 정보 업데이트
            googleUser = optionalUser.get().update(name, picture);
        } else {
            // 새 유저 생성
            googleUser = new GoogleUsersEntity(name, email, picture, "User", id);
        }

        return googleUser;
    }

    public GoogleUsersEntity loadNaverUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String accessToken = userRequest.getAccessToken().getTokenValue(); // 액세스 토큰 가져오기
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // Authorization 헤더에 Bearer 토큰 추가

        HttpEntity<String> entity = new HttpEntity<>(headers);
        // 네이버의 사용자 정보 엔드포인트 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userAttributes = response.getBody(); // 사용자 정보 받기
        logger.info("User Info: " + userAttributes);
        Map<String, Object> responseMap = (Map<String, Object>) userAttributes.get("response"); // "response" 키의 데이터 추출

        String id = (String) responseMap.get("id");
        String name = (String) responseMap.get("name");
        String email = (String) responseMap.get("email");
        String picture = (String) responseMap.get("profile_image");

        GoogleUsersEntity naverUser;
        Optional<GoogleUsersEntity> optionalUser = googleUsersRepository.findByIdentifier(id);

        if (optionalUser.isPresent()) {
            // 기존 유저 정보 업데이트
            naverUser = optionalUser.get().update(name, picture);
        } else {
            // 새 유저 생성
            naverUser = new GoogleUsersEntity(name, email, picture, "User", id);
        }

        return naverUser;
    }


    public GoogleOAuth2AuthenticationResponse handleOAuth2Authentication(String authorizationCode) {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId(googleOAuthProperties.getClientId())                 // Google 클라이언트 ID
                .clientSecret(googleOAuthProperties.getClientSecret())         // Google 클라이언트 비밀
                .redirectUri(googleOAuthProperties.getRedirectUri())           // 리다이렉트 URI
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)  // 인증 코드 그랜트 타입
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")  // 인증 URI
                .tokenUri("https://oauth2.googleapis.com/token")        // 토큰 URI
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo") // UserInfo Endpoint 추가
                .userNameAttributeName("sub") // Google UserInfo에서 사용자 ID 필드 설정
                .scope("openid", "profile", "email")      // OAuth2 스코프 설정
                .build();

        CustomOAuth2AccessToken accessToken = getAccessToken(authorizationCode, registration);

        GoogleUsersEntity oAuth2User = loadGoogleUser(new OAuth2UserRequest(registration, accessToken));


        GoogleUsersEntity user = saveOrUpdate(oAuth2User);
        MemberEntity member = new MemberEntity(user.getEmail(),user.getIdentifier());

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        member.setPassword(bCryptPasswordEncoder.encode(oAuth2User.getIdentifier()));

        saveOrUpdateMember(member);

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(member.getEmail(), user.getIdentifier())
        );
        String token = jwtUtil.generateToken(authenticate.getName());

        return new GoogleOAuth2AuthenticationResponse(user, accessToken, token);
    }

    public GoogleOAuth2AuthenticationResponse handleOAuth2AuthenticationNaver(String authorizationCode) {
        String state = UUID.randomUUID().toString(); // 고유한 상태 값 생성

        ClientRegistration registration = ClientRegistration.withRegistrationId("Naver")
                .clientId(naverOAuthProperties.getClientId())                 // Google 클라이언트 ID
                .clientSecret(naverOAuthProperties.getClientSecret())         // Google 클라이언트 비밀
                .redirectUri(naverOAuthProperties.getRedirectUri())           // 리다이렉트 URI
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)  // 인증 코드 그랜트 타입
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")  // 인증 URI
                .tokenUri("https://nid.naver.com/oauth2.0/token")        // 토큰 URI
                .userInfoUri("https://openapi.naver.com/v1/nid/me") // UserInfo Endpoint 추가
                .userNameAttributeName("id") // Google UserInfo에서 사용자 ID 필드 설정
                .scope("name", "profile", "email")      // OAuth2 스코프 설정
                .build();

        CustomOAuth2AccessToken accessToken = getAccessTokenFromNaver(authorizationCode, state, registration);

        GoogleUsersEntity oAuth2User = loadNaverUser(new OAuth2UserRequest(registration, accessToken));


        GoogleUsersEntity user = saveOrUpdate(oAuth2User);
        MemberEntity member = new MemberEntity(user.getEmail(),user.getIdentifier());

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        member.setPassword(bCryptPasswordEncoder.encode(oAuth2User.getIdentifier()));

        saveOrUpdateMember(member);

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(member.getEmail(), user.getIdentifier())
        );
        String token = jwtUtil.generateToken(authenticate.getName());

        return new GoogleOAuth2AuthenticationResponse(user, accessToken, token);
    }


    public CustomOAuth2AccessToken getAccessToken(String authorizationCode, ClientRegistration registration) {
        RestTemplate restTemplate = new RestTemplate();

        // Google의 토큰 요청 URL은 registration 객체에서 가져옵니다.
        String tokenUrl = registration.getProviderDetails().getTokenUri();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);                                // 인증 코드
        params.add("client_id", registration.getClientId());                  // 클라이언트 ID
        params.add("client_secret", registration.getClientSecret());          // 클라이언트 비밀
        params.add("redirect_uri", registration.getRedirectUri());            // 리다이렉트 URI
        params.add("grant_type", registration.getAuthorizationGrantType().getValue()); // 그랜트 타입 (authorization_code)

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 토큰 요청
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        // 응답에서 액세스 토큰과 리프레시 토큰 추출
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new IllegalStateException("Invalid token response from Google");
        }

        String accessTokenValue = (String) responseBody.get("access_token");
        String refreshTokenValue = (String) responseBody.get("refresh_token"); // 리프레시 토큰
        Set<String> refreshTokenSet = Collections.singleton(refreshTokenValue);

        // OAuth2AccessToken 생성
        return new CustomOAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessTokenValue, // 액세스 토큰
                Instant.now(), // 발급 시간
                Instant.now().plusSeconds((Integer) responseBody.get("expires_in")), // 만료 시간
                refreshTokenSet // 리프레시 토큰
        );
    }

    public CustomOAuth2AccessToken getAccessTokenFromNaver(String authorizationCode, String state, ClientRegistration registration) {
        RestTemplate restTemplate = new RestTemplate();

        // 네이버의 토큰 요청 URL
        String tokenUrl = registration.getProviderDetails().getTokenUri();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);                                // 인증 코드
        params.add("client_id", registration.getClientId());                  // 클라이언트 ID
        params.add("client_secret", registration.getClientSecret());          // 클라이언트 비밀
        params.add("redirect_uri", registration.getRedirectUri());            // 리다이렉트 URI
        params.add("grant_type", registration.getAuthorizationGrantType().getValue()); // 그랜트 타입 (authorization_code)
        params.add("state", state);                                           // CSRF 방지 state 값

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 토큰 요청
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        // 응답에서 액세스 토큰과 리프레시 토큰 추출
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new IllegalStateException("Invalid token response from Naver");
        }

        String accessTokenValue = (String) responseBody.get("access_token");
        String refreshTokenValue = (String) responseBody.get("refresh_token"); // 리프레시 토큰
        Long expiresIn = Long.parseLong(responseBody.get("expires_in").toString());          // 만료 시간

        // 리프레시 토큰이 없을 경우 빈 Set으로 처리
        Set<String> refreshTokenSet = refreshTokenValue != null ? Collections.singleton(refreshTokenValue) : Collections.emptySet();

        // OAuth2AccessToken 생성
        return new CustomOAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessTokenValue, // 액세스 토큰
                Instant.now(), // 발급 시간
                Instant.now().plusSeconds(expiresIn != null ? expiresIn : 3600), // 만료 시간 (기본값: 1시간)
                refreshTokenSet // 리프레시 토큰
        );
    }


    public CustomOAuth2AccessToken refreshAccessToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleOAuthProperties.getClientId());  // 클라이언트 ID
        params.add("client_secret", googleOAuthProperties.getClientSecret());  // 클라이언트 비밀
        params.add("refresh_token", refreshToken);  // 리프레시 토큰
        params.add("grant_type", "refresh_token");  // 그랜트 타입: refresh_token

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 구글의 토큰 서버에 refresh 토큰을 보내서 새로운 액세스 토큰을 받음
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new IllegalStateException("Failed to refresh access token.");
        }

        String accessTokenValue = (String) responseBody.get("access_token");
        String refreshTokenValue = (String) responseBody.get("refresh_token"); // 리프레시 토큰
        Set<String> refreshTokenSet = Collections.singleton(refreshTokenValue);

        return new CustomOAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessTokenValue,
                Instant.now(),
                Instant.now().plusSeconds((Integer) responseBody.get("expires_in")),  // 새로운 액세스 토큰의 만료 시간
                refreshTokenSet  // 새로운 리프레시 토큰 (있다면)
        );
    }

    public CustomOAuth2AccessToken refreshAccessTokenNaver(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", naverOAuthProperties.getClientId());  // 네이버 클라이언트 ID
        params.add("client_secret", naverOAuthProperties.getClientSecret());  // 네이버 클라이언트 비밀
        params.add("refresh_token", refreshToken);  // 리프레시 토큰
        params.add("grant_type", "refresh_token");  // 그랜트 타입: refresh_token

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 네이버의 토큰 서버에 refresh 토큰을 보내서 새로운 액세스 토큰을 받음
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new IllegalStateException("Failed to refresh access token from Naver.");
        }

        String accessTokenValue = (String) responseBody.get("access_token");
        String newRefreshTokenValue = (String) responseBody.get("refresh_token"); // 새로운 리프레시 토큰 (있다면)
        Integer expiresInSeconds = Integer.parseInt((String) responseBody.get("expires_in"));  // 만료 시간 (초)

        Set<String> refreshTokenSet = newRefreshTokenValue != null
                ? Collections.singleton(newRefreshTokenValue)
                : Collections.singleton(refreshToken);

        return new CustomOAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessTokenValue,
                Instant.now(),
                Instant.now().plusSeconds(expiresInSeconds),
                refreshTokenSet
        );
    }

    // userRepository.save(user)에서 오류 해결
    private GoogleUsersEntity saveOrUpdate(GoogleUsersEntity usersEntity) {
        GoogleUsersEntity user = (GoogleUsersEntity) googleUsersRepository.findByEmail(usersEntity.getEmail())
                .map(entity -> entity.update(usersEntity.getName(), usersEntity.getPicture()))
                .orElse(usersEntity);

        return googleUsersRepository.save(user); // Save method should return GoogleUsers, not Object.
    }

    private MemberEntity saveOrUpdateMember(MemberEntity usersEntity) {
        MemberEntity user = (MemberEntity) memberRepository.findByEmail(usersEntity.getEmail())
                .map(entity -> entity.update(usersEntity.getName(), usersEntity.getPicture()))
                .orElse(usersEntity);
        user.login(true);

        return memberRepository.save(user); // Save method should return GoogleUsers, not Object.
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return null;
    }
}
