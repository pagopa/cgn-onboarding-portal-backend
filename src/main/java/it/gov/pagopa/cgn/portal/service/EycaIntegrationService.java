package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.api.EycaIntegrationApi;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.ApiResponseEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.DiscountRequestEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.InlineResponse200EycaIntegration;
import org.springframework.stereotype.Service;

@Service
public class EycaIntegrationService {


    private final EycaIntegrationApi eycaIntegrationApi;

    public EycaIntegrationService(ConfigProperties configProperties, EycaIntegrationApi eycaIntegrationApi) {
        this.eycaIntegrationApi = eycaIntegrationApi;
        this.eycaIntegrationApi.getApiClient().setBasePath(configProperties.getAttributeAuthorityBaseUrl());
    }

    public InlineResponse200EycaIntegration authorize(String username, String password){
        return eycaIntegrationApi.authentication(username, password);
    };

    public ApiResponseEycaIntegration createDiscount   (
            DiscountRequestEycaIntegration discountRequestEycaIntegration) {
        return eycaIntegrationApi.createDiscount(discountRequestEycaIntegration);
    }

}
