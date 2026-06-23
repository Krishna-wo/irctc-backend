package com.irctc.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // This defines the global security scheme — Bearer JWT
        // Once configured here, every endpoint shows a lock icon in Swagger UI
        // Click the lock → paste your JWT → all requests include it automatically
        return new OpenAPI()
                .info(new Info()
                        .title("IRCTC Backend API")
                        .description("Railway ticket booking system — built with Spring Boot 3")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your@email.com")))
                // Add JWT bearer auth to swagger globally
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                // This tells Swagger UI to add
                                // "Authorization: Bearer <token>"
                                // to every request automatically
                        ));
    }
}