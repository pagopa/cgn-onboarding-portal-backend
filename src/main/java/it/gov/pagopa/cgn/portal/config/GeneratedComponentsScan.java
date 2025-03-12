package it.gov.pagopa.cgn.portal.config;

import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.AttributeAuthorityApi;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.DefaultApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"it.gov.pagopa.cgn.portal", "it.gov.pagopa.cgnonboardingportal"})
public class GeneratedComponentsScan {

    @Bean
    public AttributeAuthorityApi attributeAuthorityApi() {
        return new AttributeAuthorityApi();
    }

    @Bean
    public DefaultApi defaultApi() {
        return new DefaultApi();
    }

    @Bean
    public EycaApi EycaApi() {
        return new EycaApi();
    }


}
