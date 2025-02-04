package com.example.LifeMaster_BE.UserManager.Peristalsis.Kakao;

import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.LoginType;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final MemberRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code) {
        // 1. 카카오에서 Access Token을 가져옴
        String accessToken = kakaoOAuthService.getAccessToken(code);

        // 2. Access Token으로 카카오 사용자 정보 가져오기
        Map<String, Object> kakaoUserInfo = kakaoOAuthService.getUserInfo(accessToken);
        String email = (String) ((Map<String, Object>) kakaoUserInfo.get("kakao_account")).get("email");
        String nickname = (String) ((Map<String, Object>) kakaoUserInfo.get("properties")).get("nickname");
        String profileUrl = (String) ((Map<String, Object>) kakaoUserInfo.get("properties")).get("profile_image");

        // 3. DB에서 사용자 조회 (이메일 기반)
        MemberEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            // 사용자 없으면 새로 저장
            MemberEntity newUser = MemberEntity.builder()
                    .email(email)
                    .nickname(nickname)
                    .imageUrl(profileUrl)
                    .loginType(LoginType.KAKAO)
                    .loginRole(LoginRole.USER) // 기본 권한
                    .build();
            return userRepository.save(newUser);
        });

        // 4. 액세스 토큰과 리프레시 토큰 생성
        String accessTokenGenerated = jwtTokenProvider.createAccessToken(user.getId());
        String refreshTokenGenerated = jwtTokenProvider.createRefreshToken();

        // 5. 리프레시 토큰을 안전한 저장소에 보관 (예: 데이터베이스)

        // 6. JWT와 리프레시 토큰 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessTokenGenerated);
        response.put("refreshToken", refreshTokenGenerated);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {

            Long userId = getUserIdFromRefreshToken(refreshToken);
            String newAccessToken = jwtTokenProvider.createAccessToken(userId);

            // 3. 새로운 액세스 토큰을 반환
            return ResponseEntity.ok().body("New Access Token: " + newAccessToken);

    }

    public Long getUserIdFromRefreshToken(String refreshToken) {
        // 리프레시 토큰에서 Claims 추출
        Claims claims = Jwts.parserBuilder()
                .setSigningKey("921473d7f670df7a9151a8c9e8070cc5") // 비밀 키 사용
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        // Claims에서 userId를 추출하여 반환
        return Long.valueOf(claims.getSubject()); // subject에 userId가 저장되어 있다고 가정
    }
}

