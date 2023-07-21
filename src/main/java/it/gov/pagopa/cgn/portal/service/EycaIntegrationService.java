package it.gov.pagopa.cgn.portal.service;


import com.google.common.net.HttpHeaders;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.api.EycaIntegrationApi;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.ApiResponseEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.DiscountRequestEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.InlineResponse200EycaIntegration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
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

    void webClient(){
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                .build();

    }



}
