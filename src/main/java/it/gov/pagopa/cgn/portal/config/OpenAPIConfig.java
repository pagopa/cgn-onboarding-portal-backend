package it.gov.pagopa.cgn.portal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class OpenAPIConfig {

    @Value("${cgn.role.header}")
    private String cgnRoleHeader;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                              .title("API Documentation")
                              .version("1.0")
                              .description("Documentazione delle API con autenticazione JWT e custom header"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth").addList("CustomHeader"))
                .components(new io.swagger.v3.oas.models.Components()
                                    .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                            .type(Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT"))
                                    .addSecuritySchemes("CustomHeader", new SecurityScheme()
                                            .type(Type.APIKEY)
                                            .name(cgnRoleHeader)  // Nome del tuo header
                                            .in(In.HEADER)));
    }
}
