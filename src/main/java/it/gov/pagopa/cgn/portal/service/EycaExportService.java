package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.exception.EycaAuthenticationException;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ApiResponseEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.UpdateDataExportEyca;
import org.springframework.stereotype.Service;

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

   public ApiResponseEyca createDiscountWithAuthorization(DataExportEyca dataExportEyca, String type) {
        String authResponse = eycaApi.authentication();

        if (authResponse.contains("ERR")){
            throw new EycaAuthenticationException(authResponse);
        }

       int colonIndex = authResponse.indexOf(':');
       String sessionId = authResponse.substring(colonIndex + 1).trim();
       apiClient.addDefaultCookie("ccdb_session", sessionId);
       return eycaApi.createDiscount(type, dataExportEyca);

    }


    public ApiResponseEyca updateDiscountWithAuthorization(UpdateDataExportEyca updateDataExportEyca, String type) {
        String authResponse = eycaApi.authentication();

        if (authResponse.contains("ERR")){
            throw new EycaAuthenticationException(authResponse);
        }

        int colonIndex = authResponse.indexOf(':');
        String sessionId = authResponse.substring(colonIndex + 1).trim();
        apiClient.addDefaultCookie("ccdb_session", sessionId);
        return eycaApi.updateDiscount(type, updateDataExportEyca);

    }

}

