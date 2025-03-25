package it.gov.pagopa.cgn.portal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("dev")
public class OpenAPIConfig {

    private static final String ROOT = "/";

    @Value("${cgn.role.header}")
    private String cgnRoleHeader;

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI().info(new Info().title("API Documentation")
                                            .version("1.0")
                                            .description(
                                                    "Documentazione delle API con autenticazione JWT e custom header"))
                            .servers(List.of(new Server().url(ROOT)))
                            .addSecurityItem(new SecurityRequirement().addList("Bearer").addList("UserRole"))
                            .components(new Components().addSecuritySchemes("Bearer",
                                                                            new SecurityScheme().type(Type.HTTP)
                                                                                                .scheme("bearer")
                                                                                                .bearerFormat("JWT"))
                                                        .addSecuritySchemes("UserRole",
                                                                            new SecurityScheme().type(Type.APIKEY)
                                                                                                .name(cgnRoleHeader)
                                                                                                .in(In.HEADER)));
    }
}
