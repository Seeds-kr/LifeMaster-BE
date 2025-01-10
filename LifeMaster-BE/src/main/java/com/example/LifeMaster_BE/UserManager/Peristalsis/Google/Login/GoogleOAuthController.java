package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2AccessToken;
import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("googleLogin")
public class GoogleOAuthController {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    private GoogleOAuthProperties googleOAuthProperties;

    public GoogleOAuthController(CustomOAuth2UserService customOAuth2UserService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Public 페이지", description = "로그인 없이 접근할 수 있는 공개 페이지")
    @GetMapping("/public")
    public String home() {
        return "This is public Page";
    }

    @Operation(summary = "Private 페이지", description = "로그인 후 접근할 수 있는 비공개 페이지")
    @GetMapping("/private/privatePage")
    public String privatePage() {
        return "This is private Page";
    }

    @Operation(
            summary = "구글 OAuth2 Callback 처리",
            description = "구글 인증 코드로 액세스 토큰과 유저 정보를 가져와 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 유저와 토큰을 반환"),
                    @ApiResponse(responseCode = "400", description = "잘못된 인증 코드")
            }
    )
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleOAuth2Callback(
            @Parameter(description = "구글 인증 코드", required = true)
            @RequestParam("code") String authorizationCode,
            Model model) {

        GoogleOAuth2AuthenticationResponse response = customOAuth2UserService.handleOAuth2Authentication(authorizationCode);

        // 유저와 토큰 가져오기
        GoogleUsersEntity user = response.getUser();
        CustomOAuth2AccessToken token = response.getToken();
        String jwtToken = response.getJwtToken();

        // JSON 형태로 유저 정보와 토큰 반환
        Map<String, Object> responseMap = Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "picture", user.getPicture()
                ),
                "token", token,
                "jwtToken", jwtToken
        );

        return ResponseEntity.ok(responseMap);
    }

    @Operation(
            summary = "Google OAuth2 인증 URL 생성",
            description = "구글 OAuth2 인증을 위한 URL을 생성하여 반환합니다."
    )
    @GetMapping("/authUrl")
    public String getAuthorizationUrl() {
        String clientId = googleOAuthProperties.getClientId(); // Google Client ID
        String redirectUri = googleOAuthProperties.getRedirectUri(); // 리다이렉트 URI
        String scope = "openid profile email"; // 접근 권한: 이메일, 프로필 정보
        String state = UUID.randomUUID().toString(); // 고유한 상태 값 생성
        String authUrl = String.format(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s&access_type=offline&include_granted_scopes=true",
                clientId, redirectUri, scope, state
        );

        return authUrl;
    }

    @Operation(
            summary = "리프레시 토큰을 사용하여 액세스 토큰 갱신",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 갱신합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "새로운 액세스 토큰 반환"),
                    @ApiResponse(responseCode = "400", description = "잘못된 리프레시 토큰")
            }
    )
    @PostMapping("/refreshToken")
    public ResponseEntity<Map<String, Object>> refreshAccessToken(
            @Parameter(description = "리프레시 토큰", required = true)
            @RequestParam("refreshToken") String refreshToken) {

        // 리프레시 토큰을 사용하여 새로운 액세스 토큰 요청
        CustomOAuth2AccessToken newToken = customOAuth2UserService.refreshAccessToken(refreshToken);

        // 새로운 액세스 토큰과 리프레시 토큰을 반환
        Map<String, Object> responseMap = Map.of(
                "token", newToken
        );

        return ResponseEntity.ok(responseMap);
    }

    @Operation(
            summary = "예외 처리",
            description = "OAuth2 인증 예외가 발생했을 때 처리하는 메소드"
    )
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public String handleOAuth2AuthenticationException(OAuth2AuthenticationException ex, Model model) {
        // 예외 출력
        ex.printStackTrace();
        model.addAttribute("error", ex.getMessage());
        return "error"; // 커스텀 에러 페이지
    }
}

