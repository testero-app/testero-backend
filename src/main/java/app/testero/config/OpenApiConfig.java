package app.testero.config;

import app.testero.config.openapi.RecordSchemaModelConverter;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Testero API")
                        .description("REST API for Testero — test administration for educational settings")
                        .version("v1"))
                // Declared explicitly: springdoc otherwise derives it from the incoming request,
                // which would put a host (and, under test, a random port) into the published spec.
                // The relative path is the servlet context path, so it holds wherever it is served.
                .addServersItem(new Server()
                        .url("/api")
                        .description("Testero API, relative to the host serving it"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token here")));
    }

    /**
     * Declares which response fields are guaranteed and which may be null, so that clients can
     * generate types from the spec instead of hand-writing them.
     */
    @Bean
    public ModelConverter recordSchemaModelConverter() {
        return new RecordSchemaModelConverter();
    }
}
