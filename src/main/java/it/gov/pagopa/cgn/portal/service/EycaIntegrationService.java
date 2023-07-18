package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.api.EycaIntegrationApi;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.ApiResponseEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.DiscountRequestEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.InlineResponse200EycaIntegration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class EycaIntegrationService {


    private final EycaIntegrationApi eycaIntegrationApi;

    public EycaIntegrationService(ConfigProperties configProperties, EycaIntegrationApi eycaIntegrationApi) {
        this.eycaIntegrationApi = eycaIntegrationApi;
        this.eycaIntegrationApi.getApiClient().setBasePath(configProperties.getAttributeAuthorityBaseUrl());
    }

    public InlineResponse200EycaIntegration authorize(String username, String password){
        return eycaIntegrationApi.authentication(username, password);
    };

    public ApiResponseEycaIntegration createDiscount   (
            DiscountRequestEycaIntegration discountRequestEycaIntegration) {
        return eycaIntegrationApi.createDiscount(discountRequestEycaIntegration);
    }


    @PostConstruct
    public void init() {
    /*    log.info("INIT " + this.getClass().getSimpleName());
        MAX_RETRIES = Integer.parseInt(configBean.getConfig(Configurations.NODO_MAX_RETRIES));
        RETRY_DELAY_MS = Integer.parseInt(configBean.getConfig(Configurations.NODO_RETRY_DELAY_MS));
        nodoApi = new NodoApi(new ApiClient()
                .setBasePath(getNodoBasePath())
                .setConnectTimeout(Integer.parseInt(configBean.getConfig(Configurations.NODO_TIMEOUT_MS))));
        log.info("END INIT " + this.getClass().getSimpleName());*/
    }

    public ApiResponseEycaIntegration createDiscountWithAuthorization(
            String username, String password, DiscountRequestEycaIntegration discountRequestEycaIntegration) {

        ApiClient apiClient = eycaIntegrationApi.getApiClient();
        apiClient.setBasePath("");

        // Eseguire l'autenticazione
        InlineResponse200EycaIntegration authResponse = eycaIntegrationApi.authentication(username, password);

        // Ottenere il cookie di sessione dalla risposta di autenticazione
        String sessionId = authResponse.getSessionId();

        // Aggiungere il cookie di sessione alle richieste successive
       // apiClient.getCookieParams().put("session-id", Collections.singletonList(sessionId));

        // Effettuare la chiamata a createDiscount con l'autenticazione tramite cookie
        return eycaIntegrationApi.createDiscount(discountRequestEycaIntegration);
    }


}
