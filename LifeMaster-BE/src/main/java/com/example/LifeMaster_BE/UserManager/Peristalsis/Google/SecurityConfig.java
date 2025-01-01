package com.example.LifeMaster_BE.UserManager.Peristalsis.Google;

import com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2MemberService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .cors(Customizer.withDefaults()) // CORS 설정 기본값 사용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("googleLogin/private/**").authenticated() // /private/** 경로는 인증 필요
                        .anyRequest().permitAll() // 나머지 요청은 모두 허용
                )
                .exceptionHandling(e -> e
                        .accessDeniedPage("/error") // 접근 거부 시 /error 페이지로 리다이렉트
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/googleLogin/loginForm") // 로그인 페이지 설정
                        .defaultSuccessUrl("/privatePage", true) // 로그인 성공 시 이동할 페이지 설정
                        .failureUrl("/error") // 실패 시 이동할 URL 설정
                        .userInfoEndpoint(userInfo -> userInfo
                               .userService(oAuth2MemberService) // 사용자 정보 처리 서비스
                        )
                );
        return http.build();
    }
}
