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

@SpringBootTest
@ActiveProfiles("dev")
public class EycaIntegrationServiceTest extends IntegrationAbstractTest {


    private EycaApi eycaApi;
    private EycaExportService eycaExportService;
    private EycaDataExportRepository eycaDataExportRepository;
    private AgreementRepository agreementRepository;
    private ExportService exportService;
    private ConfigProperties configProperties;
    private DataExportEycaConverter eycaDataExportConverter;

    @BeforeEach
    void init() {
        eycaDataExportRepository = Mockito.mock(EycaDataExportRepository.class);
        agreementRepository = Mockito.mock(AgreementRepository.class);
        configProperties = Mockito.mock(ConfigProperties.class);
        eycaDataExportConverter = Mockito.mock(DataExportEycaConverter.class);

        eycaApi = Mockito.mock(EycaApi.class);
        Mockito.when(eycaApi.getApiClient()).thenReturn(Mockito.mock(ApiClient.class));
        eycaExportService = new EycaExportService(eycaApi, configProperties);
        exportService = new ExportService(agreementRepository, eycaDataExportRepository, configProperties, eycaExportService, eycaDataExportConverter );
    }


    @Test
    void provaTest(){
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());
        Mockito.when(eycaApi.authentication()).thenReturn("sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");
        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(new ApiResponseEyca());

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }








}
