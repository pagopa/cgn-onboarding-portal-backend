package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaDataExportApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ApiResponseEycaDataExport;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.RequestEycaDataExport;
import org.springframework.stereotype.Service;

@Service
public class EycaExportService {


    private final EycaDataExportApi eycaIntegrationApi;
    private final ApiClient apiClient;

    public EycaExportService(EycaDataExportApi eycaIntegrationApi) {
        this.eycaIntegrationApi = eycaIntegrationApi;
        this.apiClient=eycaIntegrationApi.getApiClient();
        this.apiClient.setUsername("andrea.rovere@dgsspa.com");
        this.apiClient.setPassword("N'EXd+{2752\"WPuL");
        this.apiClient.setBasePath("https://ccdb.eyca.org");
    }

    private String  authorize(){
        return eycaIntegrationApi.authentication();
    };

    public ApiResponseEycaDataExport createDiscount   (RequestEycaDataExport discountRequestEycaIntegration) {
        return eycaIntegrationApi.createDiscount(discountRequestEycaIntegration);
    }

   public ApiResponseEycaDataExport createDiscountWithAuthorization(RequestEycaDataExport discountRequestEycaIntegration) {
       // Eseguire l'autenticazione
        String authResponse = authorize();
        System.out.println("::::::::: authResponse: " + authResponse);

       int colonIndex = authResponse.indexOf(':');

       // Estrai la parte della stringa dopo l'indice del carattere ':' (ignorando lo spazio dopo ':')
       String sessionId = authResponse.substring(colonIndex + 1).trim();

       // Ottenere il cookie di sessione dalla risposta di autenticazione

        // Aggiungere il cookie di sessione alle richieste successive
         apiClient.addDefaultCookie("ccdb_session", sessionId);

        // Effettuare la chiamata a createDiscount con l'autenticazione tramite cookie
        return eycaIntegrationApi.createDiscount(discountRequestEycaIntegration);
    }

}

