package it.gov.pagopa.cgn.portal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.cgn.portal.customdeserializer.TrimStringModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new TrimStringModule());
        return objectMapper;
    }

}
