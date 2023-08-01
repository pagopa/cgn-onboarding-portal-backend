package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaDataExportApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ApiResponseEycaDataExport;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.RequestEycaDataExport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EycaIntegrationServiceTest {


    @Mock
    private ApiClient apiClient;

    @Mock
    private EycaDataExportApi eycaDataExportApi;

    private EycaExportService eycaExportService;

    @BeforeEach
    public void setUp() {

        // Configura il mock di eycaIntegrationApi per restituire il mock di ApiClient
    //    when(eycaIntegrationApi.getApiClient()).thenReturn(apiClient);

        // Configura il mock di ApiClient con il metodo setBasePath
    //    doNothing().when(apiClient).setBasePath(anyString());

  //      eycaIntegrationApi.setApiClient(apiClient);
        eycaDataExportApi = new EycaDataExportApi()    ;
     //   eycaIntegrationApi.setApiClient(apiClient);
        eycaExportService = new EycaExportService(eycaDataExportApi);
      }

    @Test
    public void testCreateDiscountWithAuthorization() {

        RequestEycaDataExport requestEycaDataExport = new RequestEycaDataExport();

        ApiResponseEycaDataExport actualResponse = eycaExportService.createDiscountWithAuthorization(requestEycaDataExport);

        // Verifica che il client API sia stato chiamato correttamente con il cookie di sessione
     }




}
