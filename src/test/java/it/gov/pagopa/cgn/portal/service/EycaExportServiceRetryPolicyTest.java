package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ListApiResponseEyca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EycaExportServiceRetryPolicyTest {

    private EycaApi eycaApi;
    private ParamFacade paramFacade;
    private EycaExportService eycaExportService;

    @BeforeEach
    void setUp() {
        eycaApi = mock(EycaApi.class);
        ApiClient apiClient = mock(ApiClient.class);
        when(eycaApi.getApiClient()).thenReturn(apiClient);
        paramFacade = mock(ParamFacade.class);
        ConfigProperties configProperties = mock(ConfigProperties.class);

        eycaExportService = new EycaExportService(eycaApi, configProperties, paramFacade);
    }

    @Test
    void listDiscounts_success_doesNotReadRetryPolicy() {
        ListApiResponseEyca response = new ListApiResponseEyca();
        when(eycaApi.listDiscount(any(), any())).thenReturn(response);

        assertSame(response, eycaExportService.listDiscounts(1, 1000, "json"));

        verify(paramFacade, never()).getEycaRateLimitMaxRetries();
    }

    @Test
    void listDiscounts_retriesReadRetryCountOnlyOnce() {
        ListApiResponseEyca response = new ListApiResponseEyca();
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(2);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(0L);
        when(eycaApi.listDiscount(any(), any()))
                .thenThrow(tooManyRequests())
                .thenThrow(tooManyRequests())
                .thenReturn(response);

        assertSame(response, eycaExportService.listDiscounts(1, 1000, "json"));

        verify(paramFacade, times(1)).getEycaRateLimitMaxRetries();
        verify(eycaApi, times(3)).listDiscount(any(), any());
    }

    @Test
    void listDiscounts_numericRetryAfter_waitsForSpecifiedSeconds() {
        assertEquals(3_000L, waitForRetryAfter(" 3 "));
    }

    @Test
    void listDiscounts_httpDateRetryAfter_waitsUntilSpecifiedDate() {
        String retryAfter = "Thu, 31 Dec 2099 23:59:59 GMT";
        long expectedWaitBeforeCall = Duration.between(Instant.now(Clock.systemUTC()),
                Instant.parse("2099-12-31T23:59:59Z")).toMillis();

        long actualWait = waitForRetryAfter(retryAfter);

        assertTrue(actualWait <= expectedWaitBeforeCall);
        assertTrue(actualWait >= expectedWaitBeforeCall - 5_000L);
    }

    @Test
    void listDiscounts_malformedHttpDateRetryAfter_usesConfiguredWait() {
        assertEquals(17L, waitForRetryAfter("Wed, 1 Oct 2026 07:28:00 GMT"));
    }

    @Test
    void listDiscounts_pastHttpDateRetryAfter_doesNotWait() {
        assertEquals(0L, waitForRetryAfter("Sun, 06 Nov 1994 08:49:37 GMT"));
    }

    @Test
    void listDiscounts_overflowingNumericRetryAfter_usesConfiguredWait() {
        assertEquals(17L, waitForRetryAfter("9223372036854775807"));
    }

    private long waitForRetryAfter(String retryAfter) {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(17L);
        EycaExportService spyService = spy(eycaExportService);
        doNothing().when(spyService).sleep(anyLong());
        ListApiResponseEyca response = new ListApiResponseEyca();
        when(eycaApi.listDiscount(any(), any())).thenThrow(tooManyRequests(retryAfter)).thenReturn(response);

        assertSame(response, spyService.listDiscounts(1, 1000, "json"));

        ArgumentCaptor<Long> waitCaptor = ArgumentCaptor.forClass(Long.class);
        verify(spyService).sleep(waitCaptor.capture());
        return waitCaptor.getValue();
    }

    private static HttpClientErrorException.TooManyRequests tooManyRequests() {
        return tooManyRequests(null);
    }

    private static HttpClientErrorException.TooManyRequests tooManyRequests(String retryAfter) {
        HttpHeaders headers = new HttpHeaders();
        if (retryAfter != null) {
            headers.add("Retry-After", retryAfter);
        }
        return (HttpClientErrorException.TooManyRequests) HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                headers,
                new byte[0],
                null);
    }
}
