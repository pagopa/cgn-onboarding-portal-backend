package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.DataExportEycaConverter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.EycaDataExportRepository;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("dev")
class EycaExportServiceTest extends IntegrationAbstractTest {


    private EycaApi eycaApi;
    private EycaDataExportRepository eycaDataExportRepository;
    private ExportService exportService;
    private ConfigProperties configProperties;

    private AgreementEntity agreement;

    @BeforeEach
    void init() {
        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);

        eycaDataExportRepository = Mockito.mock(EycaDataExportRepository.class);
        AgreementRepository agreementRepository = Mockito.mock(AgreementRepository.class);
        discountRepository = Mockito.mock(DiscountRepository.class);

        configProperties = Mockito.mock(ConfigProperties.class);
        eycaApi = Mockito.mock(EycaApi.class);
        Mockito.when(eycaApi.getApiClient()).thenReturn(Mockito.mock(ApiClient.class));

        DataExportEycaConverter eycaDataExportConverter = new DataExportEycaConverter();
        EycaExportService eycaExportService = new EycaExportService(eycaApi, configProperties);
        exportService = new ExportService(agreementRepository, discountRepository, eycaDataExportRepository, configProperties, eycaExportService, eycaDataExportConverter);
    }


    private void initMockitoPreconditions(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaApi.authentication()).thenReturn("sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");
    }

    @Test
    void sendCreateEycaDiscounts_OK(){
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);
        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

@Test
    void sendCreateEycaDiscountsPartailResponse0_OK(){
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_0();

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);
        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void sendCreateEycaDiscountsPartailResponse_OK(){
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_1();

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);
        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void sendCreateEycaDiscountsPartailResponse2_OK(){
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_2();

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);
        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }


    @Test
    void sendEycaDiscountsWithRealData_OK(){
        initMockitoPreconditions();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());
        Mockito.when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);
        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void sendEycaDiscountsWithRealDataThrowsException_OK(){
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenThrow(RestClientException.class);;
        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenThrow(RestClientException.class);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }



    @Test
    void sendCreateEycaDiscountsResponseNull_OK(){
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(null);
        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenReturn(null);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void sendCreateEycaDiscountsDataNull_OK(){
        initMockitoPreconditions();

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(new ArrayList<>());

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertNull(response);

    }


    @Test
    void listViewEntityEmptyReturn_NUll(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(new ArrayList<>());

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertNull(response);
    }


    @Test
    void listLANDINGPAGEwithREFERENTReturn_OK(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWIthLandingPageAndReferent());

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listWithNoDiscountType_OK(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWIthNoDiscountype());

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


    @Test
    void Test_Data_Filter_OK(){
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);
        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

}
