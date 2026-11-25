package it.gov.pagopa.cgn.portal.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.exception.EycaAuthenticationException;
import it.gov.pagopa.cgn.portal.exception.EycaRateLimitInterruptedException;
import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@Service
public class EycaExportService {

    private static final DateTimeFormatter HTTP_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM uuuu HH:mm:ss 'GMT'", Locale.US)
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int EYCA_CODE_NO_RESULTS = 104;

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

    private <T> T callWithSessionRetry(Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpClientErrorException.Unauthorized unauthorized) {
            log.warn("EYCA session rejected (401), authenticating again and retrying once");
            authenticateOnEyca();
            return call.get();
        }
    }

    private <T> T callWithRateLimitRetry(Supplier<T> call, boolean retryServerErrors) {
        RetryState state = new RetryState();
        while (true) {
            try {
                return call.get();
            } catch (HttpClientErrorException.TooManyRequests tmr) {
                log.warn("EYCA 429 response - status: {}, headers: {}, body: {}",
                         tmr.getStatusCode(),
                         tmr.getResponseHeaders(),
                         tmr.getResponseBodyAsString());
                state.rateLimitAttempts = waitBeforeRetry(tmr, state, state.rateLimitAttempts, "rate limit (429)");
            } catch (HttpServerErrorException hse) {
                log.warn("EYCA 5xx response - status: {}, body: {}", hse.getStatusCode(), hse.getResponseBodyAsString());
                if (!retryServerErrors) {
                    throw hse;
                }
                state.serverErrorAttempts = waitBeforeRetry(hse, state, state.serverErrorAttempts, "server error (5xx)");
            }
        }
    }

    private int waitBeforeRetry(HttpStatusCodeException ex, RetryState state, int attempt, String reason) {
        if (state.maxRetries < 0) {
            state.maxRetries = Math.max(0, paramFacade.getEycaRateLimitMaxRetries());
        }
        if (attempt >= state.maxRetries) {
            log.error("EYCA {} persists after {} retries", reason, attempt);
            throw ex;
        }
        int nextAttempt = attempt + 1;
        long waitMs = computeWaitMs(ex);
        log.warn("EYCA {}, retry {}/{} in {} ms", reason, nextAttempt, state.maxRetries, waitMs);
        sleep(waitMs);
        return nextAttempt;
    }

    private static final class RetryState {
        private int maxRetries = -1;
        private int rateLimitAttempts;
        private int serverErrorAttempts;
    }

    private long computeWaitMs(HttpStatusCodeException ex) {
        long waitMs = Math.max(0, paramFacade.getEycaRateLimitWaitMs());
        String retryAfter = ex.getResponseHeaders() != null ? ex.getResponseHeaders().getFirst("Retry-After") : null;
        if (retryAfter != null) {
            String retryAfterValue = retryAfter.trim();
            if (retryAfterValue.matches("\\d+")) {
                try {
                    waitMs = Math.multiplyExact(Long.parseLong(retryAfterValue), 1000L);
                } catch (NumberFormatException | ArithmeticException ex1) {
                    log.warn("Unparseable Retry-After header: {}", retryAfter);
                }
            } else {
                try {
                    LocalDateTime retryDate = LocalDateTime.parse(retryAfterValue, HTTP_DATE_FORMATTER);
                    waitMs = Duration.between(Instant.now(Clock.systemUTC()), retryDate.toInstant(ZoneOffset.UTC))
                            .toMillis();
                } catch (DateTimeParseException | ArithmeticException ex1) {
                    log.warn("Unparseable Retry-After header: {}", retryAfter);
                }
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
            throw new EycaRateLimitInterruptedException("Interrupted while waiting for EYCA rate limit", ie);
        }
    }

    public void authenticateOnEyca() {
        AuthResponseEyca authResponse = eycaApi.authentication();
        AuthResponseApiResponseEyca apiResponse = authResponse == null ? null : authResponse.getApiResponse();

        if (apiResponse == null) {
            throw new EycaAuthenticationException("Empty EYCA authentication response");
        }
        if (apiResponse.getError() != AuthResponseApiResponseEyca.ErrorEnum.NUMBER_0 ||
            StringUtils.isBlank(apiResponse.getText())) {
            throw new EycaAuthenticationException("EYCA authentication failed - code: " + apiResponse.getCode() +
                                                  ", text: " + apiResponse.getText());
        }

        apiClient.setApiKey(apiResponse.getText());
    }

    public SearchResponseEyca searchDiscount(SearchDiscountRequestEyca searchDataExportEyca, String type, boolean liveN)
            throws RestClientException {
        if (liveN) {
            searchDataExportEyca.setLive(SearchDiscountRequestEyca.LiveEnum.NUMBER_0);
            log.info("Search Response with Live = N:");
        } else {
            searchDataExportEyca.setLive(SearchDiscountRequestEyca.LiveEnum.NUMBER_1);
            log.info("Search Response with Live = S");
        }
        SearchResponseEyca response = callWithSessionRetry(() -> callWithRateLimitRetry(() -> eycaApi.searchDiscount(type, searchDataExportEyca), true));

        if (Objects.nonNull(response)) {
            log.info(response.toString());
        }

        return response;
    }

    public DiscountResponseEyca listDiscounts(Integer page , Integer rows, String type) {
        ListDiscountRequestEyca listDataExportEyca = new ListDiscountRequestEyca();
        listDataExportEyca.setPage(page);
        listDataExportEyca.setRows(rows);
        try {
            return callWithSessionRetry(() -> callWithRateLimitRetry(() -> eycaApi.listDiscount(listDataExportEyca, type), true));
        } catch (HttpClientErrorException.NotFound notFound) {
            if (readApiResponseCode(notFound) != EYCA_CODE_NO_RESULTS) {
                throw notFound;
            }
            log.info("EYCA list_discount returned no discounts (404, code {})", EYCA_CODE_NO_RESULTS);
            return new DiscountResponseEyca();
        }
    }

    private static int readApiResponseCode(HttpClientErrorException ex) {
        try {
            return OBJECT_MAPPER.readTree(ex.getResponseBodyAsString()).path("api_response").path("code").asInt(-1);
        } catch (JsonProcessingException e) {
            return -1;
        }
    }

    public DiscountResponseEyca createDiscount(CreateDiscountRequestEyca dataExportEyca, String type)
            throws RestClientException {
        return callWithSessionRetry(() -> callWithRateLimitRetry(() -> eycaApi.createDiscount(dataExportEyca, type), false));
    }

    public DiscountResponseEyca updateDiscount(UpdateDiscountRequestEyca updateDataExportEyca, String type)
            throws RestClientException {
        return callWithSessionRetry(() -> callWithRateLimitRetry(() -> eycaApi.updateDiscount(updateDataExportEyca, type), true));
    }


    public DeleteDiscountResponseEyca deleteDiscount(IdRequestEyca deleteDataExportEyca, String type)
            throws RestClientException {
        return callWithSessionRetry(() -> callWithRateLimitRetry(() -> eycaApi.deleteDiscount(deleteDataExportEyca, type), true));
    }

}
