package com.branch.inventory.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI (Swagger) documentation for the Multi-Branch
 * Inventory & Transfer Management System REST API.
 *
 * Access the UI at: http://localhost:8080/swagger-ui/index.html
 * The /swagger-ui/** and /v3/api-docs/** paths are whitelisted in
 * SecurityConfig.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Multi-Branch Inventory & Transfer Management System API", version = "1.0", description = "REST API for managing multi-branch inventory, stock transfers, "
        +
        "multi-tier approval workflows, and finance reporting.", contact = @Contact(name = "Yves Mugisha", email = "yves@example.com")), servers = {
                @Server(url = "http://localhost:8080", description = "Local development"),
                @Server(url = "https://your-production-domain.com", description = "Production")
        }, security = @SecurityRequirement(name = "bearerAuth") // apply JWT globally
)
@SecurityScheme(name = "bearerAuth", description = "JWT Bearer token. Obtain from POST /api/auth/login, then prefix with 'Bearer '.", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {
    // All configuration is annotation-driven; no additional beans required.
}