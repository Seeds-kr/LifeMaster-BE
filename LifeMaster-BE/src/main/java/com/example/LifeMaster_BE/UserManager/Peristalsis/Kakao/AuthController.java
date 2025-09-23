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
        try {
            // 1. 카카오 Access Token 받기
            String kakaoAccessToken = kakaoOAuthService.getAccessToken(code);
            if (kakaoAccessToken == null) {
                return ResponseEntity.status(500).body(Map.of("message", "카카오 Access Token 발급 실패"));
            }

            // 2. 카카오 사용자 정보 조회
            Map<String, Object> kakaoUserInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken);
            if (kakaoUserInfo == null || kakaoUserInfo.get("kakao_account") == null) {
                return ResponseEntity.status(500).body(Map.of("message", "카카오 사용자 정보 조회 실패"));
            }

            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) kakaoUserInfo.get("properties");

            String email = kakaoAccount.get("email") != null ? (String) kakaoAccount.get("email") : null;
            String nickname = properties.get("nickname") != null ? (String) properties.get("nickname") : "KakaoUser";
            String profileUrl = properties.get("profile_image") != null ? (String) properties.get("profile_image") : "";

            if (email == null) {
                return ResponseEntity.status(400).body(Map.of("message", "사용자 이메일이 없습니다."));
            }

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

            // 4. JWT 발급
            String accessToken = jwtUtil.generateToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            // 5. 응답 반환
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 원인 출력
            return ResponseEntity.status(500).body(Map.of(
                    "message", "서버 오류 발생: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        try {
            // JwtUtil로 email 추출
            String email = jwtUtil.extractEmail(refreshToken);

            MemberEntity user = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

            String newAccessToken = jwtUtil.generateToken(user.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", refreshToken); // 기존 refreshToken 그대로 반환

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 원인 출력
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "message", "리프레시 토큰이 유효하지 않습니다: " + e.getMessage()
            ));
        }
    }
}
