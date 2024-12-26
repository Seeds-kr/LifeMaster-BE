package com.example.LifeMaster_BE.Config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi detoxTimeApi() {
        return GroupedOpenApi.builder()
                .group("detox-time")
                .pathsToMatch("/detox/time/**")
                .build();
    }

    @Bean
    public GroupedOpenApi PomodoroTimerApi() {
        return GroupedOpenApi.builder()
                .group("pomodoro-timer")
                .pathsToMatch("/time/pomodoro/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("LifeMaster Detox API")
                        .version("v1")
                        .description("APIs for managing detox schedules"))
                .externalDocs(new ExternalDocumentation()
                        .description("Detox API Documentation")
                        .url("https://example.com/docs"));
    }
}
