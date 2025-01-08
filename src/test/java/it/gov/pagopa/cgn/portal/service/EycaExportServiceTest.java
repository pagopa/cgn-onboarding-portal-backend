package it.gov.pagopa.cgn.portal.service;


import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.*;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.EycaDataExportRepository;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.nimbusds.jose.util.StandardCharset;

@SpringBootTest
@ActiveProfiles("dev")
class EycaExportServiceTest
        extends IntegrationAbstractTest {


    private EycaApi eycaApiMock;
    private EycaDataExportRepository eycaDataExportRepository;
    private ExportService exportService;
    private EycaExportService eycaExportServiceMock;
    private EycaExportService eycaExportService;

    private ConfigProperties configProperties;

    private AgreementEntity agreement;

    @BeforeEach
    void init() {
        Properties sessionProperties = new Properties();
        sessionProperties.put("mail.transport.protocol", "smtp");
        sessionProperties.put("mail.smtp.port", 25);
        sessionProperties.put("mail.smtp.starttls.enable", "true");
        sessionProperties.put("mail.smtp.auth", "true");

        Session s = Session.getDefaultInstance(sessionProperties);
        MimeMessage expectedMimeMessage = new MimeMessage(s);

        try {
            expectedMimeMessage.setFrom("aa@bb.cc");
            expectedMimeMessage.setRecipient(RecipientType.TO, new InternetAddress("aa@bb.cc"));
            expectedMimeMessage.setSubject("test");
            expectedMimeMessage.setText("questa è una test", StandardCharset.UTF_8.name());

        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        }

        JavaMailSender javaMailSenderMock = Mockito.mock(JavaMailSender.class);
        Mockito.when(javaMailSenderMock.createMimeMessage()).thenReturn(expectedMimeMessage);

        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE,TestUtils.FAKE_ORGANIZATION_NAME);

        eycaDataExportRepository = Mockito.mock(EycaDataExportRepository.class);
        AgreementRepository agreementRepository = Mockito.mock(AgreementRepository.class);
        discountRepository = Mockito.mock(DiscountRepository.class);

        configProperties = Mockito.mock(ConfigProperties.class);
        eycaApiMock = Mockito.mock(EycaApi.class);
        Mockito.when(eycaApiMock.getApiClient()).thenReturn(Mockito.mock(ApiClient.class));

        eycaExportService = new EycaExportService(eycaApiMock,configProperties);

        DataExportEycaWrapperConverter dataExportEycaConverter = new DataExportEycaWrapperConverter();
        UpdateDataExportEycaWrapperConverter updateDataExportEycaConverter = new UpdateDataExportEycaWrapperConverter();

        EmailNotificationFacade emailNotificationFacade = Mockito.mock(EmailNotificationFacade.class);

        eycaExportServiceMock = Mockito.mock(EycaExportService.class);

        exportService = new ExportService(agreementRepository,
                                          discountRepository,
                                          eycaDataExportRepository,
                                          configProperties,
                                          eycaExportServiceMock,
                                          dataExportEycaConverter,
                                          updateDataExportEycaConverter,
                                          emailNotificationFacade);

    }


    private void initMockitoPreconditions() {
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaApiMock.authentication())
               .thenReturn(
                       "sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");
    }

    @Test
    void sendCreateEycaDiscounts_OK() {
        initMockitoPreconditions();

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportForCreate());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void sendUpdateEycaDiscountsListEmpty_OK() {
        initMockitoPreconditions();

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportForUpdate());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();

        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();

    }

    @Test
    void sendEycaDiscountsListEmpty_OK() {
        initMockitoPreconditions();

        List<EycaDataExportViewEntity> eycaDataExportViewEntityList = Collections.emptyList();

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(eycaDataExportViewEntityList);

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void sendCreateEycaDiscountsPartialResponse0_OK() {
        initMockitoPreconditions();

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());
        Mockito.when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_0();

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void sendCreateEycaDiscountsPartialResponse1_OK() {
        initMockitoPreconditions();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());
        Mockito.when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_1();

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void sendCreateEycaDiscountsPartialResponse2_OK() {
        initMockitoPreconditions();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());
        Mockito.when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_2();

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }


    @Test
    void sendEycaDiscountsWithRealData_OK() {
        initMockitoPreconditions();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        Mockito.when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }


    @Test
    void sendDeleteEycaDiscounts_OK() {
        initMockitoPreconditions();

        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity1.setEycaUpdateId("ce00958658596");
        Mockito.when(discountRepository.findByEycaUpdateId("ce00958658596")).thenReturn(Optional.of(discountEntity1));

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity2.setEycaUpdateId("ce00958999596");
        Mockito.when(discountRepository.findByEycaUpdateId("ce00958999596")).thenReturn(Optional.of(discountEntity2));

        Mockito.when(eycaDataExportRepository.findAll())
               .thenReturn(TestUtils.getTobeDeletedEycaDataExportViewEntityList());

        DeleteApiResponseEyca apiResponseEyca = TestUtils.getDeleteApiResponse();

        Mockito.when(eycaApiMock.deleteDiscount(Mockito.anyString(), Mockito.any(DeleteDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();
    }


    @Test
    void sendEycaDiscountsWithRealDataThrowsException_OK() {
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenThrow(RestClientException.class);
        ;
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenThrow(RestClientException.class);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }


    @Test
    void deleteEycaDiscountsThrowsException() {
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll())
               .thenReturn(TestUtils.getTobeDeletedEycaDataExportViewEntityList());

        Mockito.when(eycaApiMock.deleteDiscount(Mockito.anyString(), Mockito.any(DeleteDataExportEyca.class)))
               .thenThrow(RestClientException.class);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void deleteEycaDiscountsRsponseNull_OK() {
        initMockitoPreconditions();
        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity1.setEycaUpdateId("ce00958658596");
        Mockito.when(discountRepository.findByEycaUpdateId("ce00958658596")).thenReturn(Optional.of(discountEntity1));

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity2.setEycaUpdateId("ce00958999596");
        Mockito.when(discountRepository.findByEycaUpdateId("ce00958999596")).thenReturn(Optional.of(discountEntity2));

        Mockito.when(eycaDataExportRepository.findAll())
               .thenReturn(TestUtils.getTobeDeletedEycaDataExportViewEntityList());

        Mockito.when(eycaApiMock.deleteDiscount(Mockito.anyString(), Mockito.any(DeleteDataExportEyca.class)))
               .thenReturn(null);
        ;

        exportService.sendDiscountsToEyca();
    }


    @Test
    void sendCreateEycaDiscountsResponse_OK() {
        initMockitoPreconditions();

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity.setId(1L);
        discountEntity.setState(DiscountStateEnum.PUBLISHED);
        discountEntity.setVisibleOnEyca(true);
        discountEntity.setEycaUpdateId("c34020231110173110208108");

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityListFromCSV());

        Mockito.when(discountRepository.findByEycaUpdateId("c34020231110173110208108"))
               .thenReturn(Optional.of(discountEntity));

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class))).thenReturn(null);
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(null);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void sendCreateEycaDiscountsData_OK() {
        initMockitoPreconditions();

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(new ArrayList<>());

        exportService.sendDiscountsToEyca();
    }


    @Test
    void listViewEntityEmptyReturn_OK() {
        initMockitoPreconditions();

        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(new ArrayList<>());

        exportService.sendDiscountsToEyca();
    }


    @Test
    void listLANDINGPAGEwithREFERENTReturn_OK() {
        initMockitoPreconditions();

        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWIthLandingPageAndReferent());

        exportService.sendDiscountsToEyca();
    }

    @Test
    void listWithNoDiscountType_OK() {
        initMockitoPreconditions();

        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWIthNoDiscountype());

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }


    @Test
    void sendEycaDiscounts_KO() {
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);

        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());
        Mockito.when(eycaApiMock.authentication()).thenReturn("ERROR");

        exportService.sendDiscountsToEyca();
    }

    @Test
    void sendEycaDiscounts_Exception() {
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);

        Mockito.when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(null);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void syncEycaUpdateIdOnEyca_RestClientException() {
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(true);
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenThrow(RestClientException.class);
        Mockito.when(eycaApiMock.authentication())
               .thenReturn(
                       "sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");

        exportService.sendDiscountsToEyca();
    }


    @Test
    void sendEycaDiscounts_NotAllowed() {
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(false);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void sendEycaDiscounts_NotPresent() {
        Mockito.when(configProperties.getEycaExportEnabled()).thenReturn(null);

        exportService.sendDiscountsToEyca();
    }


    @Test
    void Test_Data_Filter_OK() {
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void Test_SyncEycaUpdateId_OK() {
        initMockitoPreconditions();
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataListForSync());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        Mockito.when(discountRepository.findByEycaUpdateId(Mockito.anyString()))
               .thenReturn(TestUtils.getDiscountWithEycaUpdateId(agreement));

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseWithDataEmptyList();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                      Mockito.anyString(),
                                                      Mockito.any(Boolean.class))).thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void testBuildCsv() {
        ByteArrayResource resource = exportService.buildEycaCsv(TestUtils.getEycaDataExportViewEntityListFromCSV());
        Assert.assertFalse(resource.getByteArray().length==0);
    }

    @Test
    void testSyncOnEyca_AddItemsToDeleteOnCCDB_ok() {
        Mockito.doNothing().when(eycaExportServiceMock).authenticateOnEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(), Mockito.any(), Mockito.any(Boolean.class)))
               .thenReturn(TestUtils.getSearchApiResponseEyca());

        Mockito.when(eycaExportServiceMock.listDiscounts(Mockito.any(), Mockito.any(),Mockito.any()))
               .thenReturn(TestUtils.getListApiResponseEyca());

        List<EycaDataExportViewEntity> entityList = TestUtils.getEycaDataExportViewEntityListFromCSV();
        exportService.syncEycaUpdateIdOnEyca(entityList);

        Assertions.assertEquals(ExportService.LIVE_NO,
                                entityList.stream()
                                          .filter(d -> TestUtils.FAKE_OID_1.equals(d.getEycaUpdateId()))
                                          .findAny()
                                          .get()
                                          .getLive());
        Assertions.assertEquals(ExportService.LIVE_NO,
                                entityList.stream()
                                          .filter(d -> TestUtils.FAKE_OID_2.equals(d.getEycaUpdateId()))
                                          .findAny()
                                          .get()
                                          .getLive());
    }

    @Test
    void testSearchDiscount_LiveN_false() {
        SearchApiResponseEyca resp = TestUtils.getSearchApiResponseEyca();
        resp.getApiResponse().getData().getDiscounts().getData().get(0).setLive(1);

        Mockito.when(eycaApiMock.searchDiscount(Mockito.any(), Mockito.any()))
               .thenReturn(resp);

        SearchApiResponseEyca response = eycaExportService.searchDiscount(TestUtils.createEmptySearchDataExportEyca(),"json",false);

        Assertions.assertEquals(ExportService.LIVE_YES_INT,
                                response.getApiResponse().getData().getDiscounts().getData().get(0)
                                          .getLive());

    }

    @Test
    void testSearchDiscount_LiveN_true() {
        SearchApiResponseEyca resp = TestUtils.getSearchApiResponseEyca();
        resp.getApiResponse().getData().getDiscounts().getData().get(0).setLive(0);

        Mockito.when(eycaApiMock.searchDiscount(Mockito.any(), Mockito.any()))
               .thenReturn(resp);

        SearchApiResponseEyca response = eycaExportService.searchDiscount(TestUtils.createEmptySearchDataExportEyca(),"json",true);

        Assertions.assertEquals(ExportService.LIVE_NO_INT,
                                response.getApiResponse().getData().getDiscounts().getData().get(0)
                                        .getLive());

    }

    @Test
    void testCreateBodyAll_Ok() {
        List<String[]> rowsForCreate = new ArrayList<>();
        rowsForCreate.add( new String[]{TestUtils.FAKE_OID_1,
                                        "Amazon",
                                        "CYBERMONDAY24",
                                        "No limit",
                                        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH)),
                                        LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH))});

        List<String[]> rowsForUpdate = new ArrayList<>();

        rowsForUpdate.add( new String[]{TestUtils.FAKE_OID_2,
                                        "Uci Cinemas",
                                        "CHRISTMAS24",
                                        "No limit",
                                        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH)),
                                        LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH))});

        String body = exportService.createBody(rowsForCreate,rowsForUpdate);

        Assertions.assertTrue(body.contains("Created") && body.contains("update"));
    }

    @Test
    void testCreateBodyOneOfOrNothing_Ok() {
        List<String[]> rowsForCreate = new ArrayList<>();
        rowsForCreate.add( new String[]{TestUtils.FAKE_OID_1,
                                        "Amazon",
                                        "CYBERMONDAY24",
                                        "No limit",
                                        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH)),
                                        LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH))});

        String body = exportService.createBody(rowsForCreate,Collections.emptyList());

        Assertions.assertTrue(body.contains("Created") && !body.contains("update"));

        List<String[]> rowsForUpdate = new ArrayList<>();

        rowsForUpdate.add( new String[]{TestUtils.FAKE_OID_2,
                                        "Uci Cinemas",
                                        "CHRISTMAS24",
                                        "No limit",
                                        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH)),
                                        LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("MMM d, yyyy",Locale.ENGLISH))});

        body = exportService.createBody(Collections.emptyList(),rowsForUpdate);

        Assert.assertTrue(!body.contains("Created") && body.contains("update"));

        body = exportService.createBody(Collections.emptyList(),Collections.emptyList());

        Assert.assertTrue(!body.contains("Created") && !body.contains("update"));
    }

    @Test
    void testSendEmailToEyca() {
        initMockitoPreconditions();

        exportService.sendDiscountsToEyca();
    }
}