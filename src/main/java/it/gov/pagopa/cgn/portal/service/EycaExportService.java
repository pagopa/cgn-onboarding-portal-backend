package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.exception.EycaAuthenticationException;
import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@Service
public class EycaExportService {

    private final EycaApi eycaApi;
    private final ApiClient apiClient;
    private final ParamFacade paramFacade;

    public EycaExportService(EycaApi eycaApi, ConfigProperties configProperties, ParamFacade paramFacade) {
        this.eycaApi = eycaApi;
        this.paramFacade = paramFacade;
        this.apiClient = eycaApi.getApiClient();
        this.apiClient.setUsername(configProperties.getEycaUsername());
        this.apiClient.setPassword(configProperties.getEycaPassword());
    }

    private <T> T callWithRateLimitRetry(Supplier<T> call) {
        int maxRetries = Math.max(0, paramFacade.getEycaRateLimitMaxRetries());
        int attempt = 0;
        while (true) {
            try {
                return call.get();
            } catch (HttpClientErrorException.TooManyRequests tmr) {
                log.warn("EYCA 429 response - status: {}, headers: {}, body: {}",
                         tmr.getStatusCode(),
                         tmr.getResponseHeaders(),
                         tmr.getResponseBodyAsString());
                if (attempt >= maxRetries) {
                    log.error("EYCA rate limit (429) persists after {} retries", attempt);
                    throw tmr;
                }
                attempt++;
                long waitMs = computeWaitMs(tmr);
                log.warn("EYCA rate limit (429), retry {}/{} in {} ms", attempt, maxRetries, waitMs);
                sleep(waitMs);
            }
        }
    }

    private long computeWaitMs(HttpClientErrorException ex) {
        long waitMs = Math.max(0, paramFacade.getEycaRateLimitWaitMs());
        String retryAfter = ex.getResponseHeaders() != null ? ex.getResponseHeaders().getFirst("Retry-After") : null;
        if (retryAfter != null) {
            try {
                waitMs = Long.parseLong(retryAfter.trim()) * 1000L;
            } catch (NumberFormatException nfe) {
                log.warn("Unparseable Retry-After header: {}", retryAfter);
            }
        }
        return Math.max(waitMs, 0);
    }

    void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RestClientException("Interrupted while waiting for EYCA rate limit", ie);
        }
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
        SearchApiResponseEyca response = callWithRateLimitRetry(() -> eycaApi.searchDiscount(type, searchDataExportEyca));

        if (Objects.nonNull(response)) {
            log.info(response.toString());
        }

        return response;
    }

    public ListApiResponseEyca listDiscounts(Integer page , Integer rows, String type) {
        ListDataExportEyca listDataExportEyca = new ListDataExportEyca();
        listDataExportEyca.setPage(page);
        listDataExportEyca.setRows(rows);
        return callWithRateLimitRetry(() -> eycaApi.listDiscount(type, listDataExportEyca));
    }

    public ApiResponseEyca createDiscount(DataExportEyca dataExportEyca, String type)
            throws RestClientException {
        return callWithRateLimitRetry(() -> eycaApi.createDiscount(type, dataExportEyca));
    }

    public ApiResponseEyca updateDiscount(UpdateDataExportEyca updateDataExportEyca, String type)
            throws RestClientException {
        return callWithRateLimitRetry(() -> eycaApi.updateDiscount(type, updateDataExportEyca));
    }


    public DeleteApiResponseEyca deleteDiscount(DeleteDataExportEyca deleteDataExportEyca, String type)
            throws RestClientException {
        return callWithRateLimitRetry(() -> eycaApi.deleteDiscount(type, deleteDataExportEyca));
    }

}

