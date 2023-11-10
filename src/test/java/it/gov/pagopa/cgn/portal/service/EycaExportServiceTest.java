package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.DataExportEycaConverter;
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
    private AgreementRepository agreementRepository;
    private DiscountRepository discountRepository;


    @BeforeEach
    void init() {
        eycaDataExportRepository = Mockito.mock(EycaDataExportRepository.class);
        agreementRepository = Mockito.mock(AgreementRepository.class);
        discountRepository = Mockito.mock(DiscountRepository.class);

        configProperties = Mockito.mock(ConfigProperties.class);
        eycaApi = Mockito.mock(EycaApi.class);
        Mockito.when(eycaApi.getApiClient()).thenReturn(Mockito.mock(ApiClient.class));

        DataExportEycaConverter eycaDataExportConverter = new DataExportEycaConverter();
        EycaExportService eycaExportService = new EycaExportService(eycaApi, configProperties);
        exportService = new ExportService(agreementRepository, discountRepository, eycaDataExportRepository, configProperties, eycaExportService, eycaDataExportConverter);
    }

    private void createPreconditions(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWithLandingPageWithoutReferent());
        Mockito.when(eycaApi.authentication()).thenReturn("sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");
        DiscountEntity discountEntity = new DiscountEntity();
        Optional<DiscountEntity> discountEntityOptional = Optional.of(discountEntity);
        Mockito.when(discountRepository.findById(7l)).thenReturn(discountEntityOptional);

    }

    @Test
    void sendCreateEycaDiscounts_OK(){
        createPreconditions();

        ApiResponseEyca apiResponseEyca = new ApiResponseEyca();

        ApiResponseApiResponseEyca apiResponseApiResponseEyca = new ApiResponseApiResponseEyca();
        ApiResponseApiResponseDataEyca apiResponseDataEyca = new ApiResponseApiResponseDataEyca();
        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId("75894754th8t72vb93");

        items.add(discountItemEyca);
        apiResponseDataEyca.setDiscount(items);
        apiResponseApiResponseEyca.setData(apiResponseDataEyca);
        apiResponseEyca.setApiResponse(apiResponseApiResponseEyca);

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void sendCreateEycaDiscountsResponseNull_OK(){
        createPreconditions();

        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId("75894754th8t72vb93");

        items.add(discountItemEyca);

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(null);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void sendCreateEycaDiscountsApiResponseNull_OK(){
        createPreconditions();

        ApiResponseEyca apiResponseEyca = new ApiResponseEyca();

        ApiResponseApiResponseEyca apiResponseApiResponseEyca = new ApiResponseApiResponseEyca();
        ApiResponseApiResponseDataEyca apiResponseDataEyca = new ApiResponseApiResponseDataEyca();
        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId("75894754th8t72vb93");

        items.add(discountItemEyca);
        apiResponseDataEyca.setDiscount(items);
        apiResponseApiResponseEyca.setData(apiResponseDataEyca);
        apiResponseEyca.setApiResponse(null);

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void sendCreateEycaDiscountsDataNull_OK(){
        createPreconditions();

        ApiResponseEyca apiResponseEyca = new ApiResponseEyca();

        ApiResponseApiResponseEyca apiResponseApiResponseEyca = new ApiResponseApiResponseEyca();
        ApiResponseApiResponseDataEyca apiResponseDataEyca = new ApiResponseApiResponseDataEyca();
        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId("75894754th8t72vb93");

        items.add(discountItemEyca);
        apiResponseDataEyca.setDiscount(items);
        apiResponseApiResponseEyca.setData(null);
        apiResponseEyca.setApiResponse(apiResponseApiResponseEyca);

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void sendCreateEycaDiscountsWithReferentNoLandingPage_OK(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWithoutLandingPageWithReferent());
        Mockito.when(eycaApi.authentication()).thenReturn("sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");

        ApiResponseEyca apiResponseEyca = new ApiResponseEyca();

        ApiResponseApiResponseEyca apiResponseApiResponseEyca = new ApiResponseApiResponseEyca();
        ApiResponseApiResponseDataEyca apiResponseDataEyca = new ApiResponseApiResponseDataEyca();
        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId("75894754th8t72vb93");

        items.add(discountItemEyca);
        apiResponseDataEyca.setDiscount(items);
        apiResponseApiResponseEyca.setData(apiResponseDataEyca);
        apiResponseEyca.setApiResponse(apiResponseApiResponseEyca);

        Mockito.when(eycaApi.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void notAllowedDiscountTypeReturn_OK(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWithNotAllowedDiscounTpe());
        Mockito.when(eycaApi.authentication()).thenReturn("sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void notLiveReturn_OK(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListLiveEmptyOrNotY());
        Mockito.when(eycaApi.authentication()).thenReturn("sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

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
    void sendUpdateEycaDiscounts_OK(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWithLandingPageWithoutReferentWithEycaUpdateId());
        Mockito.when(eycaApi.authentication()).thenReturn("sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");

        ApiResponseEyca apiResponseEyca = new ApiResponseEyca();

        ApiResponseApiResponseEyca apiResponseApiResponseEyca = new ApiResponseApiResponseEyca();
        ApiResponseApiResponseDataEyca apiResponseDataEyca = new ApiResponseApiResponseDataEyca();
        List<DiscountItemEyca> items = new ArrayList<>();
        DiscountItemEyca discountItemEyca = new DiscountItemEyca();
        discountItemEyca.setId("75894754th8t72vb93");

        items.add(discountItemEyca);
        apiResponseDataEyca.setDiscount(items);
        apiResponseApiResponseEyca.setData(apiResponseDataEyca);
        apiResponseEyca.setApiResponse(apiResponseApiResponseEyca);

        Mockito.when(eycaApi.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class))).thenReturn(apiResponseEyca);

        ResponseEntity<String> response = exportService.sendDiscountsToEyca();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());


    }


    @Test
    void sendEycaDiscounts_KO(){
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);

        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWithLandingPageWithoutReferent());
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
