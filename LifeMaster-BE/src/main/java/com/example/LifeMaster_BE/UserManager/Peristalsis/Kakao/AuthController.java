package com.example.LifeMaster_BE.UserManager.Peristalsis.Kakao;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.LoginType;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

        // 2. 사용자 정보 조회
        Map<String, Object> kakaoUserInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken);
        String email = (String) ((Map<String, Object>) kakaoUserInfo.get("kakao_account")).get("email");
        String nickname = (String) ((Map<String, Object>) kakaoUserInfo.get("properties")).get("nickname");
        String profileUrl = (String) ((Map<String, Object>) kakaoUserInfo.get("properties")).get("profile_image");

        // 3. DB 조회 or 신규 생성
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

        // 4. 기존 JwtUtil을 이용해 AccessToken 생성 (email 기반)
        String accessToken = jwtUtil.generateToken(user.getEmail());

        // 5. (선택) refreshToken은 DB에 저장 후 반환
        String refreshToken = "dummy-refresh-token"; // 추후 구현

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        try {
            // 1. 리프레시 토큰에서 email 추출
            String email = getEmailFromRefreshToken(refreshToken);

            // 2. DB에서 유저 조회
            MemberEntity user = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

            // 3. 새로운 액세스 토큰 생성
            String newAccessToken = jwtUtil.generateToken(user.getEmail());

            // 4. 응답 반환
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", refreshToken); // 기존 리프레시 토큰 그대로 반환
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "message", "리프레시 토큰이 유효하지 않습니다."
            ));
        }
    }

    // 리프레시 토큰에서 email 추출
    private String getEmailFromRefreshToken(String refreshToken) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtUtil.getSecretKey())// 리프레시 토큰 전용 키
                .build()
                .parseClaimsJws(refreshToken)
                .getBody()
                .getSubject();
    }

}
