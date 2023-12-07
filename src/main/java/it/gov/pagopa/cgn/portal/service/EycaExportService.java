package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.exception.EycaAuthenticationException;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class EycaExportService {

   private final EycaApi eycaApi;
    private final ApiClient apiClient;

    public EycaExportService(EycaApi eycaApi, ConfigProperties configProperties) {
        this.eycaApi = eycaApi;
        this.apiClient=eycaApi.getApiClient();
        this.apiClient.setUsername(configProperties.getEycaUsername());
        this.apiClient.setPassword(configProperties.getEycaPassword());
    }

   public ApiResponseEyca createDiscount(DataExportEyca dataExportEyca, String type) throws RestClientException {
       return eycaApi.createDiscount(type, dataExportEyca);
    }


    public void authenticateOnEyca() {
        String authResponse = eycaApi.authentication();

        if (authResponse.contains("ERR")){
            throw new EycaAuthenticationException(authResponse);
        }

        int colonIndex = authResponse.indexOf(':');
        String sessionId = authResponse.substring(colonIndex + 1).trim();
        apiClient.addDefaultCookie("ccdb_session", sessionId);
    }


    public ApiResponseEyca updateDiscount(UpdateDataExportEyca updateDataExportEyca, String type) throws RestClientException{
        return eycaApi.updateDiscount(type, updateDataExportEyca);
   }


}

