package com.example.LifeMaster_BE.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

//swagger ui 그룹화 설정(최신 버전에는 그룹화가 강제)
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LifeMaster API")
                        .version("1.0")
                        .description("LifeMaster 백엔드 API 명세서"))
                .servers(Arrays.asList(
                        new Server().url("http://ec2-54-180-88-104.ap-northeast-2.compute.amazonaws.com:8080").description("Development Server"),
                        // 여기 주소만 변경
                        //new Server().url("https://api.lifemaster.harvester.kr").description("Development Server"),
                        new Server().url("http://localhost:7550").description("Local Server"),
                        new Server().url("http://localhost:8080").description("Local Server 2")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    //Challenge Api
    @Bean
    public GroupedOpenApi ChallengeApi() {
        return GroupedOpenApi.builder()
                .group("Challenge")
                .pathsToMatch("/challenge/**")
                .build();
    }

    @Bean
    public GroupedOpenApi detoxTimeApi() {
        return GroupedOpenApi.builder()
                .group("detox-time")
                .pathsToMatch("/detox/time/**")
                .build();
    }

    //FunctionManager Api
    @Bean
    public GroupedOpenApi scheduleCalendarApi() {
        return GroupedOpenApi.builder()
                .group("schedule-calendar")
                .pathsToMatch("/calendar/**")
                .build();
    }

    @Bean
    public GroupedOpenApi todoApi() {
        return GroupedOpenApi.builder()
                .group("todo")
                .pathsToMatch("/schedule/todo/**")
                .build();
    }

    //SelfDevelop Api
    @Bean
    public GroupedOpenApi selfDevelopApi() {
        return GroupedOpenApi.builder()
                .group("self-develop")
                .pathsToMatch("/schedule/self-reflection/**")
                .build();
    }

    //TimeManager Api
    @Bean
    public GroupedOpenApi alarmApi() {
        return GroupedOpenApi.builder()
                .group("alarm")
                .pathsToMatch("/time/alarm/**")
                .build();
    }

    @Bean
    public GroupedOpenApi pomodoroTimerApi() {
        return GroupedOpenApi.builder()
                .group("pomodoro-timer")
                .pathsToMatch("/time/pomodoro/**")
                .build();
    }

    @Bean
    public GroupedOpenApi whiteNoiseApi() {
        return GroupedOpenApi.builder()
                .group("whiteNoise")
                .pathsToMatch("/time/sleep/playlist/**")
                .build();
    }

    @Bean
    public GroupedOpenApi SleepApi() {
        return GroupedOpenApi.builder()
                .group("Sleep")
                .pathsToMatch("/sleep/**")
                .build();
    }

    @Bean
    public GroupedOpenApi googleLoginApi() {
        return GroupedOpenApi.builder()
                .group("google-login")
                .pathsToMatch("/googleLogin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi naverLoginApi() {
        return GroupedOpenApi.builder()
                .group("naver-login")
                .pathsToMatch("/naverLogin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .pathsToMatch("/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi passwordApi() {
        return GroupedOpenApi.builder()
                .group("password")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupApi() {
        return GroupedOpenApi.builder()
                .group("group")
                .pathsToMatch("/group/**")
                .build();
    }

    @Bean
    public GroupedOpenApi pollApi() {
        return GroupedOpenApi.builder()
                .group("poll")
                .pathsToMatch("/community/improvePost/poll/**")
                .build();
    }

    @Bean
    public GroupedOpenApi postApi() {
        return GroupedOpenApi.builder()
                .group("post-comment")
                .pathsToMatch("/posts/**")
                .build();
    }

    @Bean
    public GroupedOpenApi commentLikeApi() {
        return GroupedOpenApi.builder()
                .group("comment-like")
                .pathsToMatch("/comments/like/**")
                .build();
    }

    @Bean
    public GroupedOpenApi emailSendApi() {
        return GroupedOpenApi.builder()
                .group("email-send")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reportApi() {
        return GroupedOpenApi.builder()
                .group("report")
                .pathsToMatch("/reports")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("payment")
                .pathsToMatch("/payments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi myPageApi(){
        return GroupedOpenApi.builder()
                .group("my-page")
                .pathsToMatch("/users/me")
                .build();
    }

    @Bean
    public GroupedOpenApi usersApi(){
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/users/**")
                .build();
    }
    @Bean
    public GroupedOpenApi s3Api() {
        return GroupedOpenApi.builder()
                .group("s3")
                .pathsToMatch("/s3/**")
                .build();
    }
}
