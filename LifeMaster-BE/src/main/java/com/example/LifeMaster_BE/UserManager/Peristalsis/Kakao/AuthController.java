package com.example.LifeMaster_BE.UserManager.Peristalsis.Kakao;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.LoginType;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code) {
        // 1. 카카오 Access Token 받기
        String kakaoAccessToken = kakaoOAuthService.getAccessToken(code);

        // 2. 카카오 사용자 정보 조회
        Map<String, Object> kakaoUserInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken);
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
        Map<String, Object> properties = (Map<String, Object>) kakaoUserInfo.get("properties");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) properties.get("nickname");
        String profileUrl = (String) properties.get("profile_image");

        // 3. DB 조회 혹은 신규 생성
        MemberEntity user = memberRepository.findByEmail(email).orElseGet(() -> {
            MemberEntity newUser = MemberEntity.builder()
                    .email(email)
                    .nickname(nickname)
                    .imageUrl(profileUrl)
                    .loginType(LoginType.KAKAO)
                    .loginRole(LoginRole.USER)
                    .build();
            return memberRepository.save(newUser);
        });

        // 4. JwtUtil로 우리 서버용 JWT 발급
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail()); // JwtUtil 안에서 refresh 토큰 생성 구현

        // 5. 토큰 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        try {
            // 1. JwtUtil로 refreshToken에서 email 추출
            String email = jwtUtil.extractEmail(refreshToken);

            // 2. DB에서 사용자 조회
            MemberEntity user = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

            // 3. 새로운 access token 발급
            String newAccessToken = jwtUtil.generateToken(user.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", refreshToken); // 기존 refreshToken 그대로 반환
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "message", "리프레시 토큰이 유효하지 않습니다."
            ));
        }
    }
}
