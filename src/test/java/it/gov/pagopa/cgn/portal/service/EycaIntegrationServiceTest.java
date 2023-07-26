package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgnonboardingportal.eycaintegration.api.EycaIntegrationApi;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.ApiResponseEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.DiscountRequestEycaIntegration;
import it.gov.pagopa.cgnonboardingportal.eycaintegration.model.InlineResponse200EycaIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EycaIntegrationServiceTest {

    @Mock
    private ApiClient apiClient;

    @Mock
    private EycaIntegrationApi eycaIntegrationApi;

    private EycaIntegrationService eycaIntegrationService;

    @BeforeEach
    public void setUp() {

        // Configura il mock di eycaIntegrationApi per restituire il mock di ApiClient
    //    when(eycaIntegrationApi.getApiClient()).thenReturn(apiClient);

        // Configura il mock di ApiClient con il metodo setBasePath
    //    doNothing().when(apiClient).setBasePath(anyString());

  //      eycaIntegrationApi.setApiClient(apiClient);
        eycaIntegrationApi = new EycaIntegrationApi();
     //   eycaIntegrationApi.setApiClient(apiClient);
        eycaIntegrationService = new EycaIntegrationService(eycaIntegrationApi);
    }

    @Test
    public void testCreateDiscountWithAuthorization() {
        // Mock della risposta di autenticazione
        // Configurazione del mock del client API per restituire la risposta di autenticazione
      //  when(eycaIntegrationApi.authentication(anyString(), anyString())).thenReturn(authResponse);

        // Mock della risposta della chiamata a createDiscount
        ApiResponseEycaIntegration expectedResponse = new ApiResponseEycaIntegration();
      //  when(eycaIntegrationApi.createDiscount(any(DiscountRequestEycaIntegration.class))).thenReturn(expectedResponse);

        // Chiamata al metodo da testare con parametri di esempio
        String username = "test-username";
        String password = "test-password";
        DiscountRequestEycaIntegration discountRequest = new DiscountRequestEycaIntegration();
        ApiResponseEycaIntegration actualResponse = eycaIntegrationService.createDiscountWithAuthorization(username, password, discountRequest);

        // Verifica che il client API sia stato chiamato correttamente con il cookie di sessione
        verify(eycaIntegrationApi).authentication(username, password);
        verify(eycaIntegrationApi).createDiscount(discountRequest);

        // Verifica che la risposta sia stata restituita correttamente
        assertEquals(expectedResponse, actualResponse);
    }



}
