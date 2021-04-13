package it.gov.pagopa.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConfigProperties {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${exception_generic_message}")
    private String exceptionGenericMessage;


    public boolean isActiveProfileDev() {
        return "dev".equals(getActiveProfile());
    }
}
