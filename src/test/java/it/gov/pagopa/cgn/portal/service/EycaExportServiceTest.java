package it.gov.pagopa.cgn.portal.service;


import com.nimbusds.jose.util.StandardCharset;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.DataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.converter.DeleteDataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.converter.UpdateDataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("dev")
class EycaExportServiceTest
        extends IntegrationAbstractTest {


    private EycaApi eycaApiMock;
    private EycaDataExportRepository eycaDataExportRepository;
    private ExportService exportService;
    private ExportService exportServiceMock;
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

        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                EntityType.PRIVATE,
                                                                TestUtils.FAKE_ORGANIZATION_NAME);

        eycaDataExportRepository = Mockito.mock(EycaDataExportRepository.class);
        AgreementRepository agreementRepository = Mockito.mock(AgreementRepository.class);
        discountRepository = Mockito.mock(DiscountRepository.class);

        configProperties = Mockito.mock(ConfigProperties.class);
        eycaApiMock = Mockito.mock(EycaApi.class);
        Mockito.when(eycaApiMock.getApiClient()).thenReturn(Mockito.mock(ApiClient.class));

        eycaExportService = new EycaExportService(eycaApiMock, configProperties);

        DataExportEycaWrapperConverter dataExportEycaConverter = new DataExportEycaWrapperConverter();
        UpdateDataExportEycaWrapperConverter updateDataExportEycaConverter = new UpdateDataExportEycaWrapperConverter();
        DeleteDataExportEycaWrapperConverter deleteDataExportEycaConverter = new DeleteDataExportEycaWrapperConverter();

        EmailNotificationFacade emailNotificationFacade = Mockito.mock(EmailNotificationFacade.class);

        eycaExportServiceMock = Mockito.mock(EycaExportService.class);
        exportServiceMock = Mockito.mock(ExportService.class);

        exportService = new ExportService(agreementRepository,
                                          discountRepository,
                                          eycaDataExportRepository,
                                          configProperties,
                                          eycaExportServiceMock,
                                          dataExportEycaConverter,
                                          updateDataExportEycaConverter,
                                          deleteDataExportEycaConverter,
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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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

        Mockito.when(eycaApiMock.createDiscount(Mockito.anyString(), Mockito.any(DataExportEyca.class)))
               .thenReturn(null);
        Mockito.when(eycaApiMock.updateDiscount(Mockito.anyString(), Mockito.any(UpdateDataExportEyca.class)))
               .thenReturn(null);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(SearchDataExportEyca.class),
                                                          Mockito.anyString(),
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenThrow(RestClientException.class);
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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

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
                                                          Mockito.any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void testBuildCsv() {
        ByteArrayResource resource = exportService.buildEycaCsv(TestUtils.getEycaDataExportViewEntityListFromCSV());
        Assert.assertFalse(resource.getByteArray().length==0);
    }

    @Test
    void testSyncOnEyca_AddItemsToDeleteOnCCDB_ok() {
        doNothing().when(eycaExportServiceMock).authenticateOnEyca();
        Mockito.when(eycaExportServiceMock.searchDiscount(Mockito.any(), Mockito.any(), Mockito.any(Boolean.class)))
               .thenReturn(TestUtils.getSearchApiResponseEyca());

        Mockito.when(eycaExportServiceMock.listDiscounts(Mockito.any(), Mockito.any(), Mockito.any()))
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

        Mockito.when(eycaApiMock.searchDiscount(Mockito.any(), Mockito.any())).thenReturn(resp);

        SearchApiResponseEyca response = eycaExportService.searchDiscount(TestUtils.createEmptySearchDataExportEyca(),
                                                                          "json",
                                                                          false);

        Assertions.assertEquals(ExportService.LIVE_YES_INT,
                                response.getApiResponse().getData().getDiscounts().getData().get(0).getLive());

    }

    @Test
    void testSearchDiscount_LiveN_true() {
        SearchApiResponseEyca resp = TestUtils.getSearchApiResponseEyca();
        resp.getApiResponse().getData().getDiscounts().getData().get(0).setLive(0);

        Mockito.when(eycaApiMock.searchDiscount(Mockito.any(), Mockito.any())).thenReturn(resp);

        SearchApiResponseEyca response = eycaExportService.searchDiscount(TestUtils.createEmptySearchDataExportEyca(),
                                                                          "json",
                                                                          true);

        Assertions.assertEquals(ExportService.LIVE_NO_INT,
                                response.getApiResponse().getData().getDiscounts().getData().get(0).getLive());

    }

    @Test
    void testCreateBodyOneOfOrAllOrNothing_Ok() {
        List<String[]> rowsForCreate = new ArrayList<>();
        rowsForCreate.add(new String[]{TestUtils.FAKE_OID_1,
                                       "Amazon",
                                       "CYBERMONDAY24",
                                       "No limit",
                                       LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy",
                                                                                          Locale.ENGLISH)),
                                       LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("MMM d, yyyy",
                                                                                                       Locale.ENGLISH))});

        String bodyC = exportService.createBody(rowsForCreate, Collections.emptyList(), Collections.emptyList());

        List<String[]> rowsForUpdate = new ArrayList<>();
        rowsForUpdate.add(new String[]{TestUtils.FAKE_OID_2,
                                       "Uci Cinemas",
                                       "CHRISTMAS24",
                                       "No limit",
                                       LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy",
                                                                                          Locale.ENGLISH)),
                                       LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("MMM d, yyyy",
                                                                                                       Locale.ENGLISH))});

        String bodyU = exportService.createBody(Collections.emptyList(), rowsForUpdate, Collections.emptyList());

        List<String[]> rowsForDelete = new ArrayList<>();
        rowsForDelete.add(new String[]{TestUtils.FAKE_OID_3,
                                       "Uci Cinemas",
                                       "CHRISTMAS25",
                                       "No limit",
                                       LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy",
                                                                                          Locale.ENGLISH)),
                                       LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("MMM d, yyyy",
                                                                                                       Locale.ENGLISH))});

        String bodyD = exportService.createBody(Collections.emptyList(), Collections.emptyList(), rowsForDelete);

        //oneof
        assertTrue(bodyC.contains("Created") && !bodyC.contains("update") && !bodyC.contains("delete"));
        assertTrue(!bodyU.contains("Created") && bodyU.contains("update") && !bodyU.contains("delete"));
        assertTrue(!bodyD.contains("Created") && !bodyD.contains("update") && bodyD.contains("delete"));

        //all
        String bodyA = exportService.createBody(rowsForCreate, rowsForUpdate, rowsForDelete);
        assertTrue(bodyA.contains("Created") && bodyA.contains("update") && bodyA.contains("delete"));

        //nothing
        String bodyN = exportService.createBody(Collections.emptyList(),
                                                Collections.emptyList(),
                                                Collections.emptyList());
        assertTrue(!bodyN.contains("Created") && !bodyN.contains("update") && !bodyN.contains("delete"));

    }

    @Test
    void sendDeleteEycaDiscountsWithNADate_KO() {
        initMockitoPreconditions();

        List<EycaDataExportViewEntity> entities = TestUtils.getTobeDeletedEycaDataExportViewEntityList();
        entities.getFirst().setEndDate(null);
        entities.getFirst().setStartDate(null);

        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(entities);

        DeleteApiResponseEyca apiResponseEyca = TestUtils.getDeleteApiResponse();

        Mockito.when(eycaApiMock.deleteDiscount(Mockito.anyString(), Mockito.any(DeleteDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();
    }

    @Test
    void testSendDiscountsToEyca_ExceptionSetsToDeleteFromEycaAdmin() {

        List<EycaDataExportViewEntity> entities = TestUtils.getTobeDeletedEycaDataExportViewEntityList();
        List<DataExportEycaWrapper<DeleteDataExportEyca>> wrappers = exportService.getWrappersToDeleteOnEyca(entities);

        Mockito.when(eycaDataExportRepository.findAll())
               .thenReturn(TestUtils.getTobeDeletedEycaDataExportViewEntityList());

        when(exportServiceMock.getWrappersToDeleteOnEyca(Mockito.any())).thenReturn(wrappers);

        Mockito.when(eycaExportServiceMock.deleteDiscount(Mockito.any(), Mockito.any()))
               .thenThrow(new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));

        exportService.sendDiscountsToEyca();
    }

    @Test
    void testSendDiscountsToEyca_createItemViewToDeleteFromEycaAdmin() {
        initMockitoPreconditions();

        List<EycaDataExportViewEntity> entities = TestUtils.getTobeDeletedEycaDataExportViewEntityList();
        List<DataExportEycaWrapper<DeleteDataExportEyca>> wrappers = exportService.getWrappersToDeleteOnEyca(entities);

        Mockito.when(eycaDataExportRepository.findAll())
               .thenReturn(new ArrayList<>(TestUtils.getTobeDeletedEycaDataExportViewEntityList()));

        when(exportServiceMock.getWrappersToDeleteOnEyca(Mockito.any())).thenReturn(wrappers);

        Mockito.when(eycaExportServiceMock.listDiscounts(Mockito.any(), Mockito.any(), Mockito.any()))
               .thenReturn(TestUtils.getListApiResponseEyca());

        Mockito.when(eycaExportServiceMock.deleteDiscount(Mockito.any(), Mockito.any()))
               .thenThrow(new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));

        exportService.sendDiscountsToEyca();
    }

    @Test
    void sendDiscountsToEyca_shouldUpdateDiscount_whenEycaEmailUpdateRequiredIsTrue() {
        initMockitoPreconditions();
        ExportService exportServiceSpy = Mockito.spy(exportService);


        DiscountEntity disc = TestUtils.createSampleDiscountEntity(agreement);
        disc.setVisibleOnEyca(true);
        disc.setEycaUpdateId("EYCA123");
        disc.setEycaEmailUpdateRequired(true);
        disc.setState(DiscountStateEnum.PUBLISHED);
        disc.setStartDate(LocalDate.now());
        disc.setEndDate(LocalDate.now().plusDays(10));
        disc.setEycaLandingPageUrl("xxx");
        disc.setVisibleOnEyca(true);

        doNothing().when(exportServiceSpy).syncEycaUpdateIdOnEyca(ArgumentMatchers.<EycaDataExportViewEntity>anyList());

        Mockito.when(discountRepository.findByEycaUpdateId(Mockito.any())).thenReturn(Optional.of(disc));


        EycaDataExportViewEntity viewEntity = new EycaDataExportViewEntity();
        viewEntity.setEycaUpdateId(disc.getEycaUpdateId());
        viewEntity.setLive(ExportService.LIVE_YES);
        viewEntity.setStartDate(LocalDate.now());
        viewEntity.setEndDate(LocalDate.now().plusDays(10));
        viewEntity.setEycaEmailUpdateRequired(true);
        viewEntity.setDiscountType(DiscountCodeTypeEnum.LANDINGPAGE.getEycaDataCode());

        // simulate that only update list contains data
        Mockito.when(eycaDataExportRepository.findAll()).thenReturn(List.of(viewEntity));

        exportServiceSpy.sendDiscountsToEyca();

        Optional<DiscountEntity> optDisc = discountRepository.findByEycaUpdateId("EYCA123");
        Assertions.assertTrue(optDisc.filter(discountEntity -> !discountEntity.getEycaEmailUpdateRequired())
                                     .isPresent());
    }


    @Test
    void testSendEmailToEyca() {
        initMockitoPreconditions();

        exportService.sendDiscountsToEyca();
    }
}