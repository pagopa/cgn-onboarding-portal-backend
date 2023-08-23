package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.exception.EycaAuthenticationException;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ApiResponseEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import org.springframework.stereotype.Service;

@Service
public class EycaExportService {

   private final EycaApi eycaApi;
    private final ApiClient apiClient;
    private final ConfigProperties configProperties;

    public EycaExportService(EycaApi eycaApi, ConfigProperties configProperties) {
        this.eycaApi = eycaApi;
        this.configProperties = configProperties;
        this.apiClient=eycaApi.getApiClient();
        this.apiClient.setUsername(configProperties.getEycaUsername());
        this.apiClient.setPassword(configProperties.getEycaPassword());
     //   this.apiClient.setBasePath(configProperties.getEycaBaseUrl());
    }

    private String authenticateOnEyca(){
            return eycaApi.authentication();
    }

    public ApiResponseEyca createDiscount(DataExportEyca discountRequestEycaIntegration, String type) {
        return eycaApi.createDiscount(type, discountRequestEycaIntegration);
    }

   public ApiResponseEyca createDiscountWithAuthorization(DataExportEyca discountRequestEycaIntegration, String type) {
        String authResponse = authenticateOnEyca();

        if (authResponse.contains("ERR")){
            throw new EycaAuthenticationException(authResponse);
        }

       int colonIndex = authResponse.indexOf(':');
       String sessionId = authResponse.substring(colonIndex + 1).trim();
       apiClient.addDefaultCookie("ccdb_session", sessionId);
       return eycaApi.createDiscount(type, discountRequestEycaIntegration);

    }

}

