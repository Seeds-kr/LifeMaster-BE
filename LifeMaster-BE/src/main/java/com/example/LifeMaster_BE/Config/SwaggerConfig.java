package com.example.LifeMaster_BE.Config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


//swagger ui 그룹화 설정(최신 버전에는 그룹화가 강제)
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    //Challenge Api
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
    public GroupedOpenApi googleLoginApi() {
        return GroupedOpenApi.builder()
                .group("google-login")
                .pathsToMatch("/googleLogin/**")
                .build();
    }


}
