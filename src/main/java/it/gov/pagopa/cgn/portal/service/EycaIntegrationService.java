package it.gov.pagopa.cgn.portal.service;


import com.google.common.net.HttpHeaders;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.api.EycaIntegrationApi;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.ApiResponseEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.DiscountRequestEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.InlineResponse200EycaIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Collections;

@Service
public class EycaIntegrationService {


    private final EycaIntegrationApi eycaIntegrationApi;

    public EycaIntegrationService(EycaIntegrationApi eycaIntegrationApi) {
        this.eycaIntegrationApi = eycaIntegrationApi;
        //this.eycaIntegrationApi.getApiClient().setBasePath(configProperties.getEycaBaseUrl());
        this.eycaIntegrationApi.getApiClient().setBasePath("https://ccdb.eyca.org");
    }

    private InlineResponse200EycaIntegration authorize(String username, String password){
        return eycaIntegrationApi.authentication(username, password);
    };

    public ApiResponseEycaIntegration createDiscount   (
            DiscountRequestEycaIntegration discountRequestEycaIntegration) {
        return eycaIntegrationApi.createDiscount(discountRequestEycaIntegration);
    }

    public ApiResponseEycaIntegration createDiscountWithAuthorization(
            String username, String password, DiscountRequestEycaIntegration discountRequestEycaIntegration) {

        ApiClient apiClient = eycaIntegrationApi.getApiClient();

        // Eseguire l'autenticazione
        InlineResponse200EycaIntegration authResponse = authorize(username, password);

        // Ottenere il cookie di sessione dalla risposta di autenticazione
        String sessionId = authResponse.getSessionId();

        // Aggiungere il cookie di sessione alle richieste successive
         apiClient.addDefaultCookie("session-id", sessionId);

        // Effettuare la chiamata a createDiscount con l'autenticazione tramite cookie
        return eycaIntegrationApi.createDiscount(discountRequestEycaIntegration);
    }

    @Autowired
    WebClient webClient;

    public Mono<Employee> create(Employee empl)
    {
        return webClient.post()
                .uri("/create/emp")
                .body(Mono.just(empl), Employee.class)
                .retrieve()
                .bodyToMono(Employee.class)
                .timeout(Duration.ofMillis(10_000));
    }

   /* void webClient(){
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                .build();

        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.method(HttpMethod.POST);
        WebClient.RequestBodySpec bodySpec = uriSpec.uri(
                uriBuilder -> uriBuilder.pathSegment("/resource").build());

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue("data");

        WebClient.ResponseSpec responseSpec = headersSpec.header(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .ifNoneMatch("*")
                .ifModifiedSince(ZonedDateTime.now())
                .retrieve();


        Mono<String> response = headersSpec.exchangeToMono(resp -> {
            if (resp.statusCode().equals(HttpStatus.OK)) {
                return resp.bodyToMono(String.class);
            } else if (resp.statusCode().is4xxClientError()) {
                return Mono.just("Error response");
            } else {
                return resp.createException()
                        .flatMap(Mono::error);
            }
        });

    }*/



}
