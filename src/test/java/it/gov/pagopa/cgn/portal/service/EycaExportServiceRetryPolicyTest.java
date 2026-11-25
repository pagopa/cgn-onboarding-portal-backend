package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.exception.EycaAuthenticationException;
import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.AuthResponseApiResponseEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.AuthResponseEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DiscountResponseEyca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        DiscountResponseEyca response = new DiscountResponseEyca();
        when(eycaApi.listDiscount(any(), any())).thenReturn(response);

        assertSame(response, eycaExportService.listDiscounts(1, 1000, "json"));

        verify(paramFacade, never()).getEycaRateLimitMaxRetries();
    }

    @Test
    void listDiscounts_retriesReadRetryCountOnlyOnce() {
        DiscountResponseEyca response = new DiscountResponseEyca();
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

    @Test
    void listDiscounts_unauthorizedThenSuccess_authenticatesAgainOnce() {
        DiscountResponseEyca response = new DiscountResponseEyca();
        when(eycaApi.authentication()).thenReturn(validAuthResponse());
        when(eycaApi.listDiscount(any(), any())).thenThrow(unauthorized()).thenReturn(response);

        assertSame(response, eycaExportService.listDiscounts(1, 1000, "json"));

        verify(eycaApi, times(1)).authentication();
        verify(eycaApi, times(2)).listDiscount(any(), any());
    }

    @Test
    void listDiscounts_unauthorizedTwice_throwsAfterOneRetry() {
        when(eycaApi.authentication()).thenReturn(validAuthResponse());
        when(eycaApi.listDiscount(any(), any())).thenThrow(unauthorized()).thenThrow(unauthorized());

        assertThrows(HttpClientErrorException.Unauthorized.class,
                     () -> eycaExportService.listDiscounts(1, 1000, "json"));

        verify(eycaApi, times(1)).authentication();
        verify(eycaApi, times(2)).listDiscount(any(), any());
    }

    @Test
    void listDiscounts_unauthorizedAndAuthenticationFails_throwsAuthenticationException() {
        when(eycaApi.authentication())
                .thenReturn(TestUtils.getAuthResponseEyca(AuthResponseApiResponseEyca.ErrorEnum.NUMBER_1,
                                                          30,
                                                          "Invalid credentials"));
        when(eycaApi.listDiscount(any(), any())).thenThrow(unauthorized());

        assertThrows(EycaAuthenticationException.class, () -> eycaExportService.listDiscounts(1, 1000, "json"));

        verify(eycaApi, times(1)).listDiscount(any(), any());
    }

    @Test
    void listDiscounts_unauthorizedThenRateLimitedThenSuccess_combinesRetries() {
        DiscountResponseEyca response = new DiscountResponseEyca();
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(0L);
        when(eycaApi.authentication()).thenReturn(validAuthResponse());
        when(eycaApi.listDiscount(any(), any()))
                .thenThrow(unauthorized())
                .thenThrow(tooManyRequests())
                .thenReturn(response);

        assertSame(response, eycaExportService.listDiscounts(1, 1000, "json"));

        verify(eycaApi, times(1)).authentication();
        verify(eycaApi, times(3)).listDiscount(any(), any());
    }

    @Test
    void updateDiscount_unauthorizedThenSuccess_authenticatesAgainOnce() {
        DiscountResponseEyca response = new DiscountResponseEyca();
        when(eycaApi.authentication()).thenReturn(validAuthResponse());
        when(eycaApi.updateDiscount(any(), any())).thenThrow(unauthorized()).thenReturn(response);

        assertSame(response, eycaExportService.updateDiscount(null, "json"));

        verify(eycaApi, times(1)).authentication();
        verify(eycaApi, times(2)).updateDiscount(any(), any());
    }

    @Test
    void listDiscounts_notFoundWithNoResultsCode_returnsEmptyResponse() {
        when(eycaApi.listDiscount(any(), any()))
                .thenThrow(notFound("{\"api_response\":{\"error\":1,\"code\":104,\"text\":\"Not found\"}}"));

        DiscountResponseEyca result = eycaExportService.listDiscounts(1, 1000, "json");

        assertNull(result.getApiResponse());
    }

    @Test
    void listDiscounts_notFoundWithOtherCode_throws() {
        when(eycaApi.listDiscount(any(), any()))
                .thenThrow(notFound("{\"api_response\":{\"error\":1,\"code\":1,\"text\":\"Not found\"}}"));

        assertThrows(HttpClientErrorException.NotFound.class, () -> eycaExportService.listDiscounts(1, 1000, "json"));
    }

    @Test
    void listDiscounts_notFoundWithUnparseableBody_throws() {
        when(eycaApi.listDiscount(any(), any())).thenThrow(notFound("Not Found"));

        assertThrows(HttpClientErrorException.NotFound.class, () -> eycaExportService.listDiscounts(1, 1000, "json"));
    }

    @Test
    void listDiscounts_serverErrorThenSuccess_retries() {
        DiscountResponseEyca response = new DiscountResponseEyca();
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(0L);
        when(eycaApi.listDiscount(any(), any()))
                .thenThrow(serverError(HttpStatus.SERVICE_UNAVAILABLE, null))
                .thenReturn(response);

        assertSame(response, eycaExportService.listDiscounts(1, 1000, "json"));

        verify(eycaApi, times(2)).listDiscount(any(), any());
    }

    @Test
    void listDiscounts_serverErrorPersists_throwsAfterRetries() {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(0L);
        when(eycaApi.listDiscount(any(), any()))
                .thenThrow(serverError(HttpStatus.INTERNAL_SERVER_ERROR, null))
                .thenThrow(serverError(HttpStatus.INTERNAL_SERVER_ERROR, null));

        assertThrows(HttpServerErrorException.class, () -> eycaExportService.listDiscounts(1, 1000, "json"));

        verify(eycaApi, times(2)).listDiscount(any(), any());
    }

    @Test
    void createDiscount_serverError_isNotRetried() {
        when(eycaApi.createDiscount(any(), any())).thenThrow(serverError(HttpStatus.INTERNAL_SERVER_ERROR, null));

        assertThrows(HttpServerErrorException.class, () -> eycaExportService.createDiscount(null, "json"));

        verify(eycaApi, times(1)).createDiscount(any(), any());
        verify(paramFacade, never()).getEycaRateLimitMaxRetries();
    }

    @Test
    void listDiscounts_rateLimitAndServerError_useSeparateCounters() {
        DiscountResponseEyca response = new DiscountResponseEyca();
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(0L);
        when(eycaApi.listDiscount(any(), any()))
                .thenThrow(tooManyRequests())
                .thenThrow(serverError(HttpStatus.BAD_GATEWAY, null))
                .thenReturn(response);

        assertSame(response, eycaExportService.listDiscounts(1, 1000, "json"));

        verify(paramFacade, times(1)).getEycaRateLimitMaxRetries();
        verify(eycaApi, times(3)).listDiscount(any(), any());
    }

    @Test
    void listDiscounts_serviceUnavailableRetryAfter_waitsForSpecifiedSeconds() {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(17L);
        EycaExportService spyService = spy(eycaExportService);
        doNothing().when(spyService).sleep(anyLong());
        DiscountResponseEyca response = new DiscountResponseEyca();
        when(eycaApi.listDiscount(any(), any()))
                .thenThrow(serverError(HttpStatus.SERVICE_UNAVAILABLE, "2"))
                .thenReturn(response);

        assertSame(response, spyService.listDiscounts(1, 1000, "json"));

        verify(spyService).sleep(2_000L);
    }

    private long waitForRetryAfter(String retryAfter) {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(17L);
        EycaExportService spyService = spy(eycaExportService);
        doNothing().when(spyService).sleep(anyLong());
        DiscountResponseEyca response = new DiscountResponseEyca();
        when(eycaApi.listDiscount(any(), any())).thenThrow(tooManyRequests(retryAfter)).thenReturn(response);

        assertSame(response, spyService.listDiscounts(1, 1000, "json"));

        ArgumentCaptor<Long> waitCaptor = ArgumentCaptor.forClass(Long.class);
        verify(spyService).sleep(waitCaptor.capture());
        return waitCaptor.getValue();
    }

    private static HttpClientErrorException.TooManyRequests tooManyRequests() {
        return tooManyRequests(null);
    }

    private static AuthResponseEyca validAuthResponse() {
        return TestUtils.getAuthResponseEyca(AuthResponseApiResponseEyca.ErrorEnum.NUMBER_0, 1, "session123");
    }

    private static HttpClientErrorException unauthorized() {
        return HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                                               "Unauthorized",
                                               new HttpHeaders(),
                                               new byte[0],
                                               null);
    }

    private static HttpClientErrorException notFound(String body) {
        return HttpClientErrorException.create(HttpStatus.NOT_FOUND,
                                               "Not Found",
                                               new HttpHeaders(),
                                               body.getBytes(StandardCharsets.UTF_8),
                                               StandardCharsets.UTF_8);
    }

    private static HttpServerErrorException serverError(HttpStatus status, String retryAfter) {
        HttpHeaders headers = new HttpHeaders();
        if (retryAfter != null) {
            headers.add("Retry-After", retryAfter);
        }
        return HttpServerErrorException.create(status, status.getReasonPhrase(), headers, new byte[0], null);
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
