package com.example.LifeMaster_BE.Security;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import com.example.LifeMaster_BE.UserManager.Peristalsis.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(Customizer.withDefaults()) // CORS 설정 빈 사용
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/user/login",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/user/register/**",
                                        //"/challenge/**",
                                        "/auth/password/reset/**",
                                        "/auth/kakao/**", "/kakao/**",
                                        "kakaoLogin/**",

                                        // OAuth2 로그인 시작/콜백
                                        "/oauth2/**",
                                        "/login/oauth2/**",

                                        //네이버
                                        "/naverLogin/callback",
                                        "/naverLogin/public",
                                        "/naverLogin/authUrl",
                                        "/naverLogin/refreshToken",

                                        //구글
                                        "/googleLogin/callback",
                                        "/googleLogin/public",
                                        "/googleLogin/authUrl",
                                        "/googleLogin/refreshToken",

                                        // 커스텀 로그인 페이지
                                        "/googleLogin/loginForm",

                                        // 실패 URL
                                        "/error").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                        //.anyRequest().permitAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("""
                                    {"status":401,"message":"로그인이 필요한 서비스입니다."}
                                    """);
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/googleLogin/loginForm")
                        .defaultSuccessUrl("/privatePage", true)
                        .failureUrl("/error")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2MemberService))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    // PasswordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정 (Swagger 및 외부 요청 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:7550",
                "http://127.0.0.1:7550","http://localhost:3000",
                "http://localhost:8080", "http://127.0.0.1:5500", "https://api.lifemaster.harvester.kr", "https://lifemaster.harvester.kr"
        )); // 허용할 API 접근 Origin 목록
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}