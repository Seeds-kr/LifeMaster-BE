package com.example.LifeMaster_BE.UserManager.Peristalsis;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleOAuth2AuthenticationResponse;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.GoogleOAuthProperties;
import com.example.LifeMaster_BE.UserManager.Peristalsis.Naver.NaverOAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
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
import com.example.LifeMaster_BE.UserManager.Member.LoginType;

import java.time.Instant;
import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {//네이버/구글 로그인 기능 관련 서비스

    @Autowired
    private GoogleOAuthProperties googleOAuthProperties;
    @Autowired
    private NaverOAuthProperties naverOAuthProperties;
    private final OAuthUsersRepository OAuthUsersRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    public CustomOAuth2UserService(OAuthUsersRepository OAuthUsersRepository, @Lazy AuthenticationManager authenticationManager, JwtUtil jwtUtil, MemberRepository memberRepository) {
        this.OAuthUsersRepository = OAuthUsersRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
    }


    //구글 유저 정보 가져오는 메소드
    public OAuthUsersEntity loadGoogleUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        RestTemplate restTemplate = new RestTemplate();

        String accessToken = userRequest.getAccessToken().getTokenValue();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userAttributes = response.getBody();
        if (userAttributes == null) {
            throw new OAuth2AuthenticationException("구글 사용자 정보가 비어있습니다.");
        }

        String id = (String) userAttributes.get("sub");   // unique id
        String name = (String) userAttributes.get("name");
        String email = (String) userAttributes.get("email");
        String picture = (String) userAttributes.get("picture");

        // 구글은 nickname 필드가 따로 없으니 name/email로 fallback
        String finalNickname = resolveNickname(null, name, email, id);

        if (id == null || id.isBlank()) {
            throw new OAuth2AuthenticationException("구글 사용자 식별자(sub)가 비어있습니다.");
        }

        Optional<OAuthUsersEntity> optionalUser = OAuthUsersRepository.findByIdentifier(id);

        OAuthUsersEntity googleUser;
        if (optionalUser.isPresent()) {
            // nickname까지 업데이트 (update 시그니처를 nickname 포함 형태로 맞춘 경우)
            googleUser = optionalUser.get().update(name, picture, finalNickname);
        } else {
            // nickname 포함 생성자 사용 (생성자 시그니처도 nickname 포함으로 맞춘 경우)
            googleUser = new OAuthUsersEntity(name, email, picture, "User", id, finalNickname);
        }

        return googleUser;
    }

    private String resolveNickname(String nickname, String name, String email, String id) {
        if (nickname != null && !nickname.isBlank()) return nickname;
        if (name != null && !name.isBlank()) return name;
        if (email != null && email.contains("@")) return email.substring(0, email.indexOf("@"));
        return "user_" + id;
    }


    //네이버 유저 정보 가져오는 메소드
    public OAuthUsersEntity loadNaverUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
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
        String nickname = (String) responseMap.get("nickname"); // ✅ 추가

        OAuthUsersEntity naverUser;
        Optional<OAuthUsersEntity> optionalUser = OAuthUsersRepository.findByIdentifier(id);

        String finalNickname = resolveNickname(nickname, name, email, id);

        if (optionalUser.isPresent()) {
            naverUser = optionalUser.get().update(name, picture, finalNickname); // ✅ 변경
        } else {
            naverUser = new OAuthUsersEntity(name, email, picture, "User", id, finalNickname); // ✅ 변경
        }

        return naverUser;
    }

    //구글 연동 인증 처리하는 메소드
    public GoogleOAuth2AuthenticationResponse handleOAuth2AuthenticationGoogle(String authorizationCode) {
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

        CustomOAuth2AccessToken accessToken = getAccessTokenGoogle(authorizationCode, registration);

        OAuthUsersEntity oAuth2User = loadGoogleUser(new OAuth2UserRequest(registration, accessToken));


        OAuthUsersEntity user = saveOrUpdate(oAuth2User);
        MemberEntity member = new MemberEntity(user.getEmail(), user.getIdentifier());
        member.setLoginType(LoginType.GOOGLE);

        Optional<MemberEntity> existing = memberRepository.findByEmail(user.getEmail());

        if (existing.isEmpty()) {
            String baseNickname = resolveNickname(
                    user.getNickname(),
                    user.getName(),
                    user.getEmail(),
                    user.getIdentifier()
            );
            member.setNickname(generateUniqueNickname(baseNickname));
        } else {
            // 기존 유저면 닉네임 유지(혹은 user.getNickname()으로 업데이트 정책 선택)
            member.setNickname(existing.get().getNickname());
        }

        member.setImageUrl(user.getPicture());

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        member.setPassword(bCryptPasswordEncoder.encode(oAuth2User.getIdentifier()));

        try {
            saveOrUpdateMember(member);
        } catch (DataIntegrityViolationException e) {
            // 닉네임 유니크 충돌 시 재시도
            String baseNickname = resolveNickname(
                    user.getNickname(), user.getName(), user.getEmail(), user.getIdentifier()
            );
            member.setNickname(generateUniqueNickname(baseNickname));
            saveOrUpdateMember(member);
        }

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(member.getEmail(), user.getIdentifier())
        );
        String token = jwtUtil.generateToken(authenticate.getName());

        return new GoogleOAuth2AuthenticationResponse(user, accessToken, token);
    }

    //네이버 연동 인증 처리하는 메소드
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

            OAuthUsersEntity oAuth2User = loadNaverUser(new OAuth2UserRequest(registration, accessToken));


            OAuthUsersEntity user = saveOrUpdate(oAuth2User);
            MemberEntity member = new MemberEntity(user.getEmail(),user.getIdentifier());
            member.setLoginType(LoginType.NAVER);

            Optional<MemberEntity> existing = memberRepository.findByEmail(user.getEmail());

            if (existing.isEmpty()) {
                String baseNickname = resolveNickname(
                        user.getNickname(),
                        user.getName(),
                        user.getEmail(),
                        user.getIdentifier()
                );
                member.setNickname(generateUniqueNickname(baseNickname));
            } else {
                // 기존 유저면 닉네임 유지(혹은 user.getNickname()으로 업데이트 정책 선택)
                member.setNickname(existing.get().getNickname());
            }

            member.setImageUrl(user.getPicture());

            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            member.setPassword(bCryptPasswordEncoder.encode(oAuth2User.getIdentifier()));

            try {
                saveOrUpdateMember(member);
            } catch (DataIntegrityViolationException e) {
                // 닉네임 유니크 충돌 시 재시도
                String baseNickname = resolveNickname(
                        user.getNickname(), user.getName(), user.getEmail(), user.getIdentifier()
                );
                member.setNickname(generateUniqueNickname(baseNickname));
                saveOrUpdateMember(member);
            }

            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(member.getEmail(), user.getIdentifier())
            );
            String token = jwtUtil.generateToken(authenticate.getName());

            return new GoogleOAuth2AuthenticationResponse(user, accessToken, token);
        }

    //구글 연동 인증 토큰 가져오는 메소드
    public CustomOAuth2AccessToken getAccessTokenGoogle(String authorizationCode, ClientRegistration registration) {
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

    //네이버 연동 인증 토큰 가져오는 메소드
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

    //리프래시 토큰으로 토큰 새로 가져오는 코드 (구글)
    public CustomOAuth2AccessToken refreshAccessTokenGoogle(String refreshToken) {
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

    //리프래시 토큰으로 토큰 새로 가져오는 코드 (네이버)
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

    //유저 정보 저장/갱신
    private OAuthUsersEntity saveOrUpdate(OAuthUsersEntity usersEntity) {
        OAuthUsersEntity user = (OAuthUsersEntity) OAuthUsersRepository.findByEmail(usersEntity.getEmail())
                .map(entity -> entity.update(usersEntity.getName(), usersEntity.getPicture(),usersEntity.getNickname()))
                .orElse(usersEntity);

        return OAuthUsersRepository.save(user); // Save method should return GoogleUsers, not Object.
    }

    private MemberEntity saveOrUpdateMember(MemberEntity usersEntity) {
        MemberEntity user = memberRepository.findByEmail(usersEntity.getEmail())
                .map(entity -> entity.updateOAuthInfo(
                        usersEntity.getPicture(),
                        usersEntity.getNickname(),
                        usersEntity.getLoginType()
                ))
                .orElse(usersEntity);

        user.login(true);
        return memberRepository.save(user);
    }

    private String generateUniqueNickname(String baseNickname) {
        if (baseNickname == null || baseNickname.isBlank()) {
            baseNickname = "user";
        }

        String nickname = baseNickname;
        int suffix = 1;

        while (memberRepository.existsByNickname(nickname)) {
            nickname = baseNickname + "_" + suffix++;
        }
        return nickname;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return null;
    }
}
