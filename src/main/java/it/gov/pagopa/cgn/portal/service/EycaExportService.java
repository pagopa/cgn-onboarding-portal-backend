package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.exception.EycaAuthenticationException;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Objects;

@Slf4j
@Service
public class EycaExportService {

    private final EycaApi eycaApi;
    private final ApiClient apiClient;

    public EycaExportService(EycaApi eycaApi, ConfigProperties configProperties) {
        this.eycaApi = eycaApi;
        this.apiClient = eycaApi.getApiClient();
        this.apiClient.setUsername(configProperties.getEycaUsername());
        this.apiClient.setPassword(configProperties.getEycaPassword());
    }

    public void authenticateOnEyca() {
        String authResponse = eycaApi.authentication();

        if (authResponse.contains("ERR")) {
            throw new EycaAuthenticationException(authResponse);
        }

        int colonIndex = authResponse.indexOf(':');
        String sessionId = authResponse.substring(colonIndex + 1).trim();
        apiClient.addDefaultCookie("ccdb_session", sessionId);
    }

    public SearchApiResponseEyca searchDiscount(SearchDataExportEyca searchDataExportEyca, String type, boolean liveN)
            throws RestClientException {
        if (liveN) {
            searchDataExportEyca.setLive(0);
            log.info("Search Response with Live = N:");
        } else {
            searchDataExportEyca.setLive(1);
            log.info("Search Response with Live = S");
        }
        SearchApiResponseEyca response = null;
        response = eycaApi.searchDiscount(type, searchDataExportEyca);

        if (Objects.nonNull(response)) {
            log.info(response.toString());
        }

        return response;
    }

    public ListApiResponseEyca listDiscounts(Integer page , Integer rows, String type) {
        ListDataExportEyca listDataExportEyca = new ListDataExportEyca();
        listDataExportEyca.setPage(page);
        listDataExportEyca.setRows(rows);
        return eycaApi.listDiscount(type, listDataExportEyca);
    }

    public ApiResponseEyca createDiscount(DataExportEyca dataExportEyca, String type)
            throws RestClientException {
        return eycaApi.createDiscount(type, dataExportEyca);
    }

    public ApiResponseEyca updateDiscount(UpdateDataExportEyca updateDataExportEyca, String type)
            throws RestClientException {
        return eycaApi.updateDiscount(type, updateDataExportEyca);
    }


    public DeleteApiResponseEyca deleteDiscount(DeleteDataExportEyca deleteDataExportEyca, String type)
            throws RestClientException {
        return eycaApi.deleteDiscount(type, deleteDataExportEyca);
    }

}

