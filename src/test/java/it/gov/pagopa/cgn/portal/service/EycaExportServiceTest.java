package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.DataExportEycaConverter;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.EycaDataExportRepository;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ApiResponseEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

@SpringBootTest
@ActiveProfiles("dev")
class EycaExportServiceTest extends IntegrationAbstractTest {


    private EycaApi eycaApi;
    private EycaDataExportRepository eycaDataExportRepository;
    private ExportService exportService;
    private ConfigProperties configProperties;

    @BeforeEach
    void init() {
        eycaDataExportRepository = Mockito.mock(EycaDataExportRepository.class);
        AgreementRepository agreementRepository = Mockito.mock(AgreementRepository.class);
        configProperties = Mockito.mock(ConfigProperties.class);
        eycaApi = Mockito.mock(EycaApi.class);
        Mockito.when(eycaApi.getApiClient()).thenReturn(Mockito.mock(ApiClient.class));

        DataExportEycaConverter eycaDataExportConverter = new DataExportEycaConverter();
        EycaExportService eycaExportService = new EycaExportService(eycaApi, configProperties);
        exportService = new ExportService(agreementRepository, eycaDataExportRepository, configProperties, eycaExportService, eycaDataExportConverter);
    }


    @Test
    void sendEycaDiscounts_OK(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());
        Mockito.when(eycaApi.authentication()).thenReturn("sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");
        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(new ApiResponseEyca());

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());


    }

    @Test
    void sendEycaDiscounts_KO(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);

        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());
        Mockito.when(eycaApi.authentication()).thenReturn("ERROR");

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }

    @Test
    void sendEycaDiscounts_NotAllowed(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(false);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertNull(response);

    }

    @Test
    void sendEycaDiscounts_NotPresent(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(null);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertNull(response);

    }


}