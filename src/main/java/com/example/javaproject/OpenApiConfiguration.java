package com.example.javaproject;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the application's OpenAPI metadata and bearer-token scheme.
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Creates the OpenAPI configuration.
     */
    public OpenApiConfiguration() {
    }

    /**
     * Builds the OpenAPI definition displayed by the API documentation.
     *
     * @return the application API definition
     */
    @Bean
    OpenAPI applicationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scam Report API")
                        .version("v1")
                        .description("""
                                API for community-submitted reports about suspicious phone numbers.
                                Warnings are generated from reports by distinct users and may expire
                                after a configurable period of inactivity. A warning is risk information,
                                not proof of wrongdoing; absence of a warning does not guarantee that a
                                phone number is safe.

                                Public operations are available without a token. Protected operations
                                require a Bearer access token obtained from `/api/v1/auth/login`.
                                Registration always creates a USER account. Administrative operations
                                require the ADMIN role; dispute moderation requires MODERATOR or ADMIN.
                                """))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token returned by POST /api/v1/auth/login.")));
    }
}
