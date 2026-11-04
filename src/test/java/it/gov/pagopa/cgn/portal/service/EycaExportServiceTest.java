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
import it.gov.pagopa.cgn.portal.exception.EycaAuthenticationException;
import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.EycaDataExportRepository;
import it.gov.pagopa.cgn.portal.scheduler.SendDiscountsToEycaJob;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.api.EycaApi;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

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
    private EmailNotificationFacade emailNotificationFacade;
    private ParamFacade paramFacade;
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

        JavaMailSender javaMailSenderMock = mock(JavaMailSender.class);
        when(javaMailSenderMock.createMimeMessage()).thenReturn(expectedMimeMessage);

        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                EntityType.PRIVATE,
                                                                TestUtils.FAKE_ORGANIZATION_NAME);

        eycaDataExportRepository = mock(EycaDataExportRepository.class);
        AgreementRepository agreementRepository = mock(AgreementRepository.class);
        discountRepository = mock(DiscountRepository.class);

        configProperties = mock(ConfigProperties.class);
        paramFacade = mock(ParamFacade.class);
        eycaApiMock = mock(EycaApi.class);
        when(eycaApiMock.getApiClient()).thenReturn(mock(ApiClient.class));

        eycaExportService = new EycaExportService(eycaApiMock, configProperties, paramFacade);

        DataExportEycaWrapperConverter dataExportEycaConverter = new DataExportEycaWrapperConverter();
        UpdateDataExportEycaWrapperConverter updateDataExportEycaConverter = new UpdateDataExportEycaWrapperConverter();
        DeleteDataExportEycaWrapperConverter deleteDataExportEycaConverter = new DeleteDataExportEycaWrapperConverter();

        emailNotificationFacade = mock(EmailNotificationFacade.class);

        eycaExportServiceMock = mock(EycaExportService.class);
        exportServiceMock = mock(ExportService.class);

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
        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        when(eycaApiMock.authentication())
               .thenReturn(
                       "sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");
    }

    @Test
    void sendCreateEycaDiscounts_OK() {
        initMockitoPreconditions();

        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportForCreate());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaApiMock, times(1)).getApiClient();
    }

    @Test
    void sendCreateEycaDiscounts_WithValidResponse_UpdatesEntityAndWrapper_OK() {
        initMockitoPreconditions();

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity.setId(1L);

        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportForCreate());
        when(discountRepository.findById(1L)).thenReturn(Optional.of(discountEntity));

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponseWithCcdbId();

        doNothing().when(eycaExportServiceMock).authenticateOnEyca();
        when(eycaExportServiceMock.listDiscounts(anyInt(), anyInt(), anyString()))
               .thenReturn(TestUtils.getListApiResponseEycaEmpty());
        when(eycaExportServiceMock.createDiscount(any(DataExportEyca.class), anyString()))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();
        
        verify(discountRepository, times(1)).findById(1L);
        verify(discountRepository, times(1)).save(any(DiscountEntity.class));
    }

    @Test
    void sendUpdateEycaDiscountsListEmpty_OK() {
        initMockitoPreconditions();

        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportForUpdate());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();

        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();

        verify(eycaExportServiceMock, times(1))
               .searchDiscount(any(SearchDataExportEyca.class),
                               anyString(),
                               any(Boolean.class));
    }

    @Test
    void sendEycaDiscountsListEmpty_OK() {
        initMockitoPreconditions();

        List<EycaDataExportViewEntity> eycaDataExportViewEntityList = Collections.emptyList();

        when(eycaDataExportRepository.findAll()).thenReturn(eycaDataExportViewEntityList);

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();

        verify(eycaApiMock, times(1)).getApiClient();
    }

    @Test
    void sendCreateEycaDiscountsPartialResponse0_OK() {
        initMockitoPreconditions();

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);

        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());
        when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_0();

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaExportServiceMock, times(2))
               .searchDiscount(any(SearchDataExportEyca.class),
                               anyString(),
                               any(Boolean.class));

    }

    @Test
    void sendCreateEycaDiscountsPartialResponse1_OK() {
        initMockitoPreconditions();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);

        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());
        when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_1();

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaExportServiceMock, times(2))
               .searchDiscount(any(SearchDataExportEyca.class),
                               anyString(),
                               any(Boolean.class));
    }

    @Test
    void sendCreateEycaDiscountsPartialResponse2_OK() {
        initMockitoPreconditions();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);

        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());
        when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        ApiResponseEyca apiResponseEyca = TestUtils.getIncompleteApiResponse_2();

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaExportServiceMock, times(2))
               .searchDiscount(any(SearchDataExportEyca.class),
                               anyString(),
                               any(Boolean.class));
    }


    @Test
    void sendEycaDiscountsWithRealData_OK() {
        initMockitoPreconditions();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        when(discountRepository.findById(500L)).thenReturn(Optional.of(discountEntity));

        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaExportServiceMock, times(2))
               .searchDiscount(any(SearchDataExportEyca.class),
                               anyString(),
                               any(Boolean.class));
    }


    @Test
    void sendDeleteEycaDiscounts_OK() {
        initMockitoPreconditions();

        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity1.setEycaUpdateId("ce00958658596");
        when(discountRepository.findByEycaUpdateId("ce00958658596")).thenReturn(Optional.of(discountEntity1));

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity2.setEycaUpdateId("ce00958999596");
        when(discountRepository.findByEycaUpdateId("ce00958999596")).thenReturn(Optional.of(discountEntity2));

        when(eycaDataExportRepository.findAll())
               .thenReturn(TestUtils.getTobeDeletedEycaDataExportViewEntityList());

        DeleteApiResponseEyca apiResponseEyca = TestUtils.getDeleteApiResponse();

        when(eycaApiMock.deleteDiscount(anyString(), any(DeleteDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaDataExportRepository, times(1)).findAll();
    }


    @Test
    void sendEycaDiscountsWithRealDataThrowsException_OK() {
        initMockitoPreconditions();
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenThrow(RestClientException.class);

        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenThrow(RestClientException.class);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();

        verify(eycaDataExportRepository, times(1)).findAll();
    }


    @Test
    void deleteEycaDiscountsThrowsException() {
        initMockitoPreconditions();
        when(eycaDataExportRepository.findAll())
               .thenReturn(TestUtils.getTobeDeletedEycaDataExportViewEntityList());

        when(eycaApiMock.deleteDiscount(anyString(), any(DeleteDataExportEyca.class)))
               .thenThrow(RestClientException.class);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaExportServiceMock, times(2))
               .deleteDiscount(any(DeleteDataExportEyca.class), anyString());
    }

    @Test
    void deleteEycaDiscountsRsponseNull_OK() {
        initMockitoPreconditions();
        DiscountEntity discountEntity1 = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity1.setEycaUpdateId("ce00958658596");
        when(discountRepository.findByEycaUpdateId("ce00958658596")).thenReturn(Optional.of(discountEntity1));

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity2.setEycaUpdateId("ce00958999596");
        when(discountRepository.findByEycaUpdateId("ce00958999596")).thenReturn(Optional.of(discountEntity2));

        when(eycaDataExportRepository.findAll())
               .thenReturn(TestUtils.getTobeDeletedEycaDataExportViewEntityList());

        when(eycaApiMock.deleteDiscount(anyString(), any(DeleteDataExportEyca.class)))
               .thenReturn(null);

        exportService.sendDiscountsToEyca();
        verify(eycaDataExportRepository, times(1)).findAll();
    }


    @Test
    void sendCreateEycaDiscountsResponse_OK() {
        initMockitoPreconditions();

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity.setId(1L);
        discountEntity.setState(DiscountStateEnum.PUBLISHED);
        discountEntity.setVisibleOnEyca(true);
        discountEntity.setEycaUpdateId("c34020231110173110208108");

        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityListFromCSV());

        when(discountRepository.findByEycaUpdateId("c34020231110173110208108"))
               .thenReturn(Optional.of(discountEntity));

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(null);
        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenReturn(null);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaApiMock, times(1)).getApiClient();
    }

    @Test
    void sendCreateEycaDiscountsData_OK() {
        initMockitoPreconditions();

        when(eycaDataExportRepository.findAll()).thenReturn(new ArrayList<>());

        exportService.sendDiscountsToEyca();
        verify(eycaDataExportRepository, times(1)).findAll();
    }


    @Test
    void listViewEntityEmptyReturn_OK() {
        initMockitoPreconditions();

        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        when(eycaDataExportRepository.findAll()).thenReturn(new ArrayList<>());

        exportService.sendDiscountsToEyca();
        verify(eycaDataExportRepository, times(1)).findAll();
    }


    @Test
    void listLANDINGPAGEwithREFERENTReturn_OK() {
        initMockitoPreconditions();

        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWIthLandingPageAndReferent());

        exportService.sendDiscountsToEyca();
        verify(eycaDataExportRepository, times(1)).findAll();
    }

    @Test
    void listWithNoDiscountType_OK() {
        initMockitoPreconditions();

        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getListWIthNoDiscountype());

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaExportServiceMock, times(1))
               .searchDiscount(any(SearchDataExportEyca.class),
                               anyString(),
                               any(Boolean.class));
    }


    @Test
    void sendEycaDiscounts_KO() {
        when(configProperties.getEycaExportEnabled()).thenReturn(true);

        when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());
        when(eycaApiMock.authentication()).thenReturn("ERROR");

        exportService.sendDiscountsToEyca();
        verify(eycaDataExportRepository, times(1)).findAll();
    }

    @Test
    void sendEycaDiscounts_Exception() {
        when(configProperties.getEycaExportEnabled()).thenReturn(true);

        when(configProperties.getEycaNotAllowedDiscountModes()).thenReturn("mode0, mode1, mode2");
        when(eycaDataExportRepository.findAll()).thenReturn(null);

        exportService.sendDiscountsToEyca();
        verify(eycaDataExportRepository, times(1)).findAll();
    }

    @Test
    void syncEycaUpdateIdOnEyca_RestClientException() {
        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenThrow(RestClientException.class);
        when(eycaApiMock.authentication())
               .thenReturn(
                       "sessionId:057c086f78cb1464c086e2cfa848cfa9a0cbfff4397452d9676e66ca8783587ab306a8e7f2bcb857c1062ab51484bcffdd6589c42e3aa373bdc76cc3ec03de86");

        exportService.sendDiscountsToEyca();

        verify(eycaApiMock, times(1)).getApiClient();
    }

    private static HttpClientErrorException tooManyRequests(String retryAfter) {
        HttpHeaders headers = new HttpHeaders();
        if (retryAfter != null) {
            headers.add("Retry-After", retryAfter);
        }
        return HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", headers, new byte[0], null);
    }

    @Test
    void searchDiscount_retriesOn429AndSucceeds() {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(2);
        EycaExportService spyService = spy(new EycaExportService(eycaApiMock, configProperties, paramFacade));
        doNothing().when(spyService).sleep(anyLong());
        SearchApiResponseEyca resp = TestUtils.getSearchApiResponseEyca();
        when(eycaApiMock.searchDiscount(any(), any())).thenThrow(tooManyRequests("1")).thenReturn(resp);

        SearchApiResponseEyca result = spyService.searchDiscount(TestUtils.createEmptySearchDataExportEyca(), "json", false);

        assertNotNull(result);
        verify(eycaApiMock, times(2)).searchDiscount(any(), any());
        verify(spyService).sleep(1000L);
    }

    @Test
    void listDiscounts_429persists_throwsAfterMaxRetries() {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(2);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(10L);
        EycaExportService spyService = spy(new EycaExportService(eycaApiMock, configProperties, paramFacade));
        doNothing().when(spyService).sleep(anyLong());
        when(eycaApiMock.listDiscount(any(), any())).thenThrow(tooManyRequests(null));

        assertThrows(HttpClientErrorException.TooManyRequests.class,
                                () -> spyService.listDiscounts(1, 1000, "json"));

        verify(eycaApiMock, times(3)).listDiscount(any(), any());
        verify(spyService, times(2)).sleep(10L);
    }

    @Test
    void retryAfter_isAlwaysRespected() {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        EycaExportService spyService = spy(new EycaExportService(eycaApiMock, configProperties, paramFacade));
        doNothing().when(spyService).sleep(anyLong());
        when(eycaApiMock.listDiscount(any(), any()))
               .thenThrow(tooManyRequests("30"))
               .thenReturn(TestUtils.getListApiResponseEyca());

        assertNotNull(spyService.listDiscounts(1, 1000, "json"));

        verify(spyService).sleep(30_000L);
    }

    @Test
    void rateLimit_withoutResponseHeaders_usesConfiguredWait() {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(7L);
        EycaExportService spyService = spy(new EycaExportService(eycaApiMock, configProperties, paramFacade));
        doNothing().when(spyService).sleep(anyLong());
        HttpClientErrorException noHeaders = HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS,
                                                                           "Too Many Requests",
                                                                           null,
                                                                           new byte[0],
                                                                           null);
        when(eycaApiMock.listDiscount(any(), any())).thenThrow(noHeaders)
                                                    .thenReturn(TestUtils.getListApiResponseEyca());

        assertNotNull(spyService.listDiscounts(1, 1000, "json"));

        verify(spyService).sleep(7L);
    }

    @Test
    void rateLimit_unparseableRetryAfter_usesConfiguredWait() {
        when(paramFacade.getEycaRateLimitMaxRetries()).thenReturn(1);
        when(paramFacade.getEycaRateLimitWaitMs()).thenReturn(7L);
        EycaExportService spyService = spy(new EycaExportService(eycaApiMock, configProperties, paramFacade));
        doNothing().when(spyService).sleep(anyLong());
        when(eycaApiMock.listDiscount(any(), any())).thenThrow(tooManyRequests("Wed, 21 Oct 2026 07:28:00 GMT"))
                                                    .thenReturn(TestUtils.getListApiResponseEyca());

        assertNotNull(spyService.listDiscounts(1, 1000, "json"));

        verify(spyService).sleep(7L);
    }

    @Test
    void sleep_nonPositive_returnsImmediately() {
        assertDoesNotThrow(() -> eycaExportService.sleep(0));
    }

    @Test
    void sleep_positive_waits() {
        assertDoesNotThrow(() -> eycaExportService.sleep(1));
    }

    @Test
    void sleep_interrupted_throwsRestClientExceptionAndKeepsInterruptFlag() {
        Thread.currentThread().interrupt();
        try {
            assertThrows(RestClientException.class, () -> eycaExportService.sleep(10));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void authenticateOnEyca_success_setsSessionCookie() {
        when(eycaApiMock.authentication()).thenReturn("OK: session123 ");

        eycaExportService.authenticateOnEyca();

        verify(eycaApiMock.getApiClient()).addDefaultCookie("ccdb_session", "session123");
    }

    @Test
    void authenticateOnEyca_errorResponse_throwsAuthenticationException() {
        when(eycaApiMock.authentication()).thenReturn("ERR: invalid credentials");

        assertThrows(EycaAuthenticationException.class, () -> eycaExportService.authenticateOnEyca());
    }

    @Test
    void createUpdateDeleteDiscount_delegateToEycaApi() {
        ApiResponseEyca createResp = new ApiResponseEyca();
        ApiResponseEyca updateResp = new ApiResponseEyca();
        DeleteApiResponseEyca deleteResp = new DeleteApiResponseEyca();
        DataExportEyca create = new DataExportEyca();
        UpdateDataExportEyca update = new UpdateDataExportEyca();
        DeleteDataExportEyca delete = new DeleteDataExportEyca();
        when(eycaApiMock.createDiscount("json", create)).thenReturn(createResp);
        when(eycaApiMock.updateDiscount("json", update)).thenReturn(updateResp);
        when(eycaApiMock.deleteDiscount("json", delete)).thenReturn(deleteResp);

        assertSame(createResp, eycaExportService.createDiscount(create, "json"));
        assertSame(updateResp, eycaExportService.updateDiscount(update, "json"));
        assertSame(deleteResp, eycaExportService.deleteDiscount(delete, "json"));
    }

    @Test
    void sendDiscountsToEyca_searchRateLimited_abortsAndNotifiesFailure() {
        initMockitoPreconditions();
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportViewEntityList());
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class), anyString(), anyBoolean()))
               .thenThrow(tooManyRequests(null));

        exportService.sendDiscountsToEyca();

        verify(eycaExportServiceMock, never()).listDiscounts(any(), any(), any());
        verify(eycaExportServiceMock, never()).createDiscount(any(), any());
        verify(emailNotificationFacade).notifyAdminForJobEycaFailure(anyString());
        verify(emailNotificationFacade, never()).notifyAdminForJobEyca(any(), any(), any(), any());
    }

    @Test
    void sendDiscountsToEyca_listRateLimited_abortsAndNotifiesFailure() {
        initMockitoPreconditions();
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportForCreate());
        when(eycaExportServiceMock.listDiscounts(any(), any(), any())).thenThrow(tooManyRequests(null));

        exportService.sendDiscountsToEyca();

        verify(eycaExportServiceMock, never()).createDiscount(any(), any());
        verify(eycaExportServiceMock, never()).deleteDiscount(any(), any());
        verify(emailNotificationFacade).notifyAdminForJobEycaFailure(anyString());
        verify(emailNotificationFacade, never()).notifyAdminForJobEyca(any(), any(), any(), any());
    }

    @Test
    void sendDiscountsToEyca_listRateLimited_notifyFailureErrorIsSwallowed() {
        initMockitoPreconditions();
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getEycaDataExportForCreate());
        when(eycaExportServiceMock.listDiscounts(any(), any(), any())).thenThrow(tooManyRequests(null));
        doThrow(new IllegalStateException("mail down")).when(emailNotificationFacade)
                                                       .notifyAdminForJobEycaFailure(anyString());

        assertDoesNotThrow(() -> exportService.sendDiscountsToEyca());
    }


    @Test
    void sendEycaDiscounts_NotAllowed() {
        when(configProperties.getEycaExportEnabled()).thenReturn(false);

        exportService.sendDiscountsToEyca();
        verify(configProperties, times(1)).getEycaExportEnabled();
    }

    @Test
    void sendEycaDiscounts_NotPresent() {
        when(configProperties.getEycaExportEnabled()).thenReturn(null);

        exportService.sendDiscountsToEyca();
        verify(configProperties, times(1)).getEycaExportEnabled();
    }


    @Test
    void Test_Data_Filter_OK() {
        initMockitoPreconditions();
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataList());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaApiMock, times(1)).getApiClient();
    }

    @Test
    void Test_SyncEycaUpdateId_OK() {
        initMockitoPreconditions();
        when(eycaDataExportRepository.findAll()).thenReturn(TestUtils.getRealDataListForSync());

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();

        when(eycaApiMock.createDiscount(anyString(), any(DataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        when(eycaApiMock.updateDiscount(anyString(), any(UpdateDataExportEyca.class)))
               .thenReturn(apiResponseEyca);
        when(discountRepository.findByEycaUpdateId(anyString()))
               .thenReturn(TestUtils.getDiscountWithEycaUpdateId(agreement));

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseWithDataEmptyList();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();

        verify(eycaApiMock, times(1)).getApiClient();
    }

    @Test
    void testBuildCsv_shouldReturnNotEmptyResource() {
        ByteArrayResource resource = exportService.buildEycaCsv(TestUtils.getEycaDataExportViewEntityListFromCSV());
        Assertions.assertEquals(106411, resource.getByteArray().length);
    }

    @Test
    void buildEycaCsv_shouldHandleExceptionAndReturnEmptyResource() {
        ExportService expService = (ExportService) ReflectionTestUtils.getField(backofficeExportFacade,
                                                                                "exportService");
        EycaDataExportViewEntity faultyEntity = mock(EycaDataExportViewEntity.class);
        when(faultyEntity.getDiscountId()).thenThrow(new RuntimeException("Test exception"));

        List<EycaDataExportViewEntity> list = List.of(faultyEntity);

        ByteArrayResource result = expService.buildEycaCsv(list);

        assertNotNull(result);
        assertEquals(0, result.contentLength());
    }

    @Test
    void testSyncOnEyca_AddItemsToDeleteOnCCDB_ok() {
        doNothing().when(eycaExportServiceMock).authenticateOnEyca();
        when(eycaExportServiceMock.searchDiscount(any(), any(), any(Boolean.class)))
               .thenReturn(TestUtils.getSearchApiResponseEyca());

        when(eycaExportServiceMock.listDiscounts(any(), any(), any()))
               .thenReturn(TestUtils.getListApiResponseEyca());

        List<EycaDataExportViewEntity> entityList = TestUtils.getEycaDataExportViewEntityListFromCSV();
        exportService.syncEycaUpdateIdOnEyca(entityList);

        assertEquals(ExportService.LIVE_NO,
                     entityList.stream()
                               .filter(d -> TestUtils.FAKE_OID_1.equals(d.getEycaUpdateId()))
                               .findAny()
                               .get()
                               .getLive());
        assertEquals(ExportService.LIVE_NO,
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

        when(eycaApiMock.searchDiscount(any(), any())).thenReturn(resp);

        SearchApiResponseEyca response = eycaExportService.searchDiscount(TestUtils.createEmptySearchDataExportEyca(),
                                                                          "json",
                                                                          false);

        assertEquals(ExportService.LIVE_YES_INT,
                     response.getApiResponse().getData().getDiscounts().getData().get(0).getLive());

    }

    @Test
    void testSearchDiscount_LiveN_true() {
        SearchApiResponseEyca resp = TestUtils.getSearchApiResponseEyca();
        resp.getApiResponse().getData().getDiscounts().getData().get(0).setLive(0);

        when(eycaApiMock.searchDiscount(any(), any())).thenReturn(resp);

        SearchApiResponseEyca response = eycaExportService.searchDiscount(TestUtils.createEmptySearchDataExportEyca(),
                                                                          "json",
                                                                          true);

        assertEquals(ExportService.LIVE_NO_INT,
                     response.getApiResponse().getData().getDiscounts().getData().get(0).getLive());

    }

   @Test
    void sendDeleteEycaDiscountsWithNADate_KO() {
        initMockitoPreconditions();

        List<EycaDataExportViewEntity> entities = TestUtils.getTobeDeletedEycaDataExportViewEntityList();
        entities.getFirst().setEndDate(null);
        entities.getFirst().setStartDate(null);

        when(eycaDataExportRepository.findAll()).thenReturn(entities);

        DeleteApiResponseEyca apiResponseEyca = TestUtils.getDeleteApiResponse();

        when(eycaApiMock.deleteDiscount(anyString(), any(DeleteDataExportEyca.class)))
               .thenReturn(apiResponseEyca);

        exportService.sendDiscountsToEyca();
        verify(eycaApiMock, times(1)).getApiClient();
    }

    @Test
    void testSendDiscountsToEyca_ExceptionSetsToDeleteFromEycaAdmin() {
        initMockitoPreconditions();

        List<EycaDataExportViewEntity> entities = TestUtils.getTobeDeletedEycaDataExportViewEntityList();
        List<DataExportEycaWrapper<DeleteDataExportEyca>> wrappers = exportService.getWrappersToDeleteOnEyca(entities);

        when(eycaDataExportRepository.findAll())
               .thenReturn(TestUtils.getTobeDeletedEycaDataExportViewEntityList());

        when(exportServiceMock.getWrappersToDeleteOnEyca(any())).thenReturn(wrappers);

        when(eycaExportServiceMock.deleteDiscount(any(), any()))
               .thenThrow(new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));

        exportService.sendDiscountsToEyca();

        verify(eycaExportServiceMock, times(2)).deleteDiscount(any(), any());
    }

    @Test
    void testSendDiscountsToEyca_createItemViewToDeleteFromEycaAdmin() {
        initMockitoPreconditions();

        List<EycaDataExportViewEntity> entities = TestUtils.getTobeDeletedEycaDataExportViewEntityList();
        List<DataExportEycaWrapper<DeleteDataExportEyca>> wrappers = exportService.getWrappersToDeleteOnEyca(entities);

        when(eycaDataExportRepository.findAll())
               .thenReturn(new ArrayList<>(TestUtils.getTobeDeletedEycaDataExportViewEntityList()));

        when(exportServiceMock.getWrappersToDeleteOnEyca(any())).thenReturn(wrappers);

        when(eycaExportServiceMock.listDiscounts(any(), any(), any()))
               .thenReturn(TestUtils.getListApiResponseEyca());

        when(eycaExportServiceMock.deleteDiscount(any(), any()))
               .thenThrow(new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));

        exportService.sendDiscountsToEyca();
        verify(eycaExportServiceMock, times(4)).deleteDiscount(any(), any());
    }

    @Test
    void sendDiscountsToEyca_shouldUpdateDiscount_whenEycaEmailUpdateRequiredIsTrue() {
        initMockitoPreconditions();
        ExportService exportServiceSpy = spy(exportService);


        DiscountEntity disc = TestUtils.createSampleDiscountEntity(agreement);
        disc.setVisibleOnEyca(true);
        disc.setEycaUpdateId("EYCA123");
        disc.setEycaEmailUpdateRequired(true);
        disc.setState(DiscountStateEnum.PUBLISHED);
        disc.setStartDate(LocalDate.now());
        disc.setEndDate(LocalDate.now().plusDays(10));
        disc.setEycaLandingPageUrl("xxx");
        disc.setVisibleOnEyca(true);

        doNothing().when(exportServiceSpy).syncEycaUpdateIdOnEyca(anyList());

        when(discountRepository.findByEycaUpdateId(any())).thenReturn(Optional.of(disc));


        EycaDataExportViewEntity viewEntity = new EycaDataExportViewEntity();
        viewEntity.setEycaUpdateId(disc.getEycaUpdateId());
        viewEntity.setLive(ExportService.LIVE_YES);
        viewEntity.setStartDate(LocalDate.now());
        viewEntity.setEndDate(LocalDate.now().plusDays(10));
        viewEntity.setEycaEmailUpdateRequired(true);
        viewEntity.setDiscountType(DiscountCodeTypeEnum.LANDINGPAGE.getEycaDataCode());

        // simulate that only update list contains data
        when(eycaDataExportRepository.findAll()).thenReturn(List.of(viewEntity));

        exportServiceSpy.sendDiscountsToEyca();

        Optional<DiscountEntity> optDisc = discountRepository.findByEycaUpdateId("EYCA123");
        Assertions.assertTrue(optDisc.filter(discountEntity -> !discountEntity.getEycaEmailUpdateRequired())
                                     .isPresent());
    }


    @Test
    void testSendEmailToEyca() {
        initMockitoPreconditions();

        exportService.sendDiscountsToEyca();

        verify(eycaApiMock, times(1)).getApiClient();
    }

    @Test
    void shouldExecuteJobAndCallExportService() {
        // Arrange
        ExportService mockExportService = mock(ExportService.class);
        JobExecutionContext mockContext = mock(JobExecutionContext.class);
        SendDiscountsToEycaJob job = new SendDiscountsToEycaJob(mockExportService);

        // Act
        job.execute(mockContext);

        // Assert
        verify(mockExportService, times(1)).sendDiscountsToEyca();
    }

    @Test
    void sendUpdateEycaDiscountsWithImageSourceWeb_OK() {
        initMockitoPreconditions();

        EycaDataExportViewEntity entity = new EycaDataExportViewEntity();
        entity.setId(1L);
        entity.setDiscountId(1L);
        entity.setEycaUpdateId("test-update-id-001");
        entity.setVendor("Test Vendor");
        entity.setName("Test Discount");
        entity.setImage("https://www.example.com/image.jpg");
        entity.setLive("Y");
        entity.setStartDate(LocalDate.of(2026, 1, 1));
        entity.setEndDate(LocalDate.of(2026, 12, 31));

        when(eycaDataExportRepository.findAll()).thenReturn(List.of(entity));

        ApiResponseEyca apiResponseEyca = TestUtils.getApiResponse();
        when(eycaExportServiceMock.updateDiscount(any(UpdateDataExportEyca.class), anyString()))
               .thenReturn(apiResponseEyca);

        SearchApiResponseEyca searchApiResponseEyca = TestUtils.getSearchApiResponseEyca();
        when(eycaExportServiceMock.searchDiscount(any(SearchDataExportEyca.class),
                                                          anyString(),
                                                          any(Boolean.class)))
               .thenReturn(searchApiResponseEyca);

        exportService.sendDiscountsToEyca();

        verify(eycaExportServiceMock, times(1)).updateDiscount(
                argThat(updateData -> updateData.getImageSourceWeb() != null &&
                                                   updateData.getImageSourceWeb().equals("https://www.example.com/image.jpg")),
                anyString()
        );
    }
}