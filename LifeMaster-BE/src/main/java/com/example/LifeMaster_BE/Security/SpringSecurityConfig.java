package com.example.LifeMaster_BE.Security;

import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService oAuth2MemberService;

    // 시큐리티 설정(API 경로 지정, filter 지정)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults()) // CORS 설정 기본값 사용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/login","/swagger-ui/**", "/user/register").permitAll() // 인증 로직이 필요 없는 url 설정
                        .requestMatchers("/private/**").authenticated()//인증 로직이 필요한 url 설정
                        .anyRequest().permitAll()//기본 인증 설정
                )
                .exceptionHandling(e -> e
                        .accessDeniedPage("/error") // 접근 거부 시 /error 페이지로 리다이렉트
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/googleLogin/loginForm") // 로그인 페이지 설정
                        .defaultSuccessUrl("/privatePage", true) // 로그인 성공 시 이동할 페이지 설정
                        .failureUrl("/error") // 실패 시 이동할 URL 설정
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2MemberService) // 사용자 정보 처리 서비스
                        )
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // AuthenticationManage 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    // 암호화 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

