package com.example.LifeMaster_BE.UserManager.Peristalsis.Google;
/*
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationSuccessHandler successHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll() // Swagger UI 접근 허용
                        .anyRequest().authenticated()             // 그 외 요청은 인증 필요
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")                   // 커스텀 로그인 페이지
                        .authorizationEndpoint(authorizationEndpoint ->
                                authorizationEndpoint.baseUri("/oauth2/authorization")) // OAuth2 설정
                        .successHandler(successHandler)        // 로그인 성공 핸들러
                );
        return http.build();
    }

    // 로그인 성공 후 처리를 담당하는 핸들러
    @Component
    public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
            response.sendRedirect("/dashboard"); // 로그인 성공 후 대시보드로 리디렉션
        }
    }
}
*/

