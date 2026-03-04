package com.example.LifeMaster_BE.Security;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {

        // JWT Bearer 인증 스키마
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://ec2-43-203-120-233.ap-northeast-2.compute.amazonaws.com:7550")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme)
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}