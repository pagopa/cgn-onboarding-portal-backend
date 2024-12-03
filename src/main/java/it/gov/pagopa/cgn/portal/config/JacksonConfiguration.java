package it.gov.pagopa.cgn.portal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.cgn.portal.customdeserializer.TrimStringModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer trimStringCustomizer() {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.modules(new TrimStringModule()).findModulesViaServiceLoader(true);
        };
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.build();
    }

}
