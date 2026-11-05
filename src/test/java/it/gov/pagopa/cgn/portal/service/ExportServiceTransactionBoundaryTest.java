package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.DataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.converter.DeleteDataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.converter.UpdateDataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.EycaDataExportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExportServiceTransactionBoundaryTest {

    private AgreementRepository agreementRepository;
    private DiscountRepository discountRepository;
    private EycaDataExportRepository eycaDataExportRepository;
    private ConfigProperties configProperties;
    private EycaExportService eycaExportService;
    private EmailNotificationFacade emailNotificationFacade;
    private PlatformTransactionManager transactionManager;
    private ExportService exportService;

    @BeforeEach
    void setUp() {
        agreementRepository = mock(AgreementRepository.class);
        discountRepository = mock(DiscountRepository.class);
        eycaDataExportRepository = mock(EycaDataExportRepository.class);
        configProperties = mock(ConfigProperties.class);
        eycaExportService = mock(EycaExportService.class);
        emailNotificationFacade = mock(EmailNotificationFacade.class);
        transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

        exportService = new ExportService(agreementRepository,
                                          discountRepository,
                                          eycaDataExportRepository,
                                          configProperties,
                                          eycaExportService,
                                          new DataExportEycaWrapperConverter(),
                                          new UpdateDataExportEycaWrapperConverter(),
                                          new DeleteDataExportEycaWrapperConverter(),
                                          emailNotificationFacade,
                                          transactionManager);
    }

    @Test
    void sendDiscountsToEyca_successSendsSummaryAfterCommit() {
        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(eycaDataExportRepository.findAll()).thenReturn(List.of());

        exportService.sendDiscountsToEyca();

        var inOrder = inOrder(transactionManager, emailNotificationFacade);
        inOrder.verify(transactionManager).commit(any(TransactionStatus.class));
        inOrder.verify(emailNotificationFacade).notifyAdminForJobEyca(anyList(),
                                                                      anyInt(),
                                                                      anyInt(),
                                                                      anyInt());
        verify(emailNotificationFacade, never()).notifyAdminForJobEycaFailure(anyString());
    }

    @Test
    void sendDiscountsToEyca_disabled_doesNotSendEmails() {
        when(configProperties.getEycaExportEnabled()).thenReturn(false);

        exportService.sendDiscountsToEyca();

        verify(emailNotificationFacade, never()).notifyAdminForJobEyca(any(), anyInt(), anyInt(), anyInt());
        verify(emailNotificationFacade, never()).notifyEycaAdmin(anyList(), anyList(), anyList());
        verify(emailNotificationFacade, never()).notifyAdminForJobEycaFailure(anyString());
    }

    @Test
    void sendDiscountsToEyca_manualEmailIsSentAfterCommit() {
        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(eycaDataExportRepository.findAll()).thenReturn(staticCodeCreateRow());
        doNothing().when(eycaExportService).authenticateOnEyca();
        when(eycaExportService.listDiscounts(anyInt(), anyInt(), anyString()))
                .thenReturn(TestUtils.getListApiResponseEycaEmpty());

        exportService.sendDiscountsToEyca();

        var inOrder = inOrder(transactionManager, emailNotificationFacade);
        inOrder.verify(transactionManager).commit(any(TransactionStatus.class));
        inOrder.verify(emailNotificationFacade).notifyAdminForJobEyca(anyList(),
                                                                      anyInt(),
                                                                      anyInt(),
                                                                      anyInt());
        inOrder.verify(emailNotificationFacade).notifyEycaAdmin(anyList(), anyList(), anyList());
        verify(emailNotificationFacade, never()).notifyAdminForJobEycaFailure(anyString());
    }

    @Test
    void sendDiscountsToEyca_commitFailureNotifiesFailureWithoutSuccessSummary() {
        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(eycaDataExportRepository.findAll()).thenReturn(staticCodeCreateRow());
        doNothing().when(eycaExportService).authenticateOnEyca();
        when(eycaExportService.listDiscounts(anyInt(), anyInt(), anyString()))
                .thenReturn(TestUtils.getListApiResponseEycaEmpty());
        doThrow(new IllegalStateException("commit failed")).when(transactionManager)
                                                           .commit(any(TransactionStatus.class));

        exportService.sendDiscountsToEyca();

        verify(emailNotificationFacade).notifyAdminForJobEycaFailure(anyString());
        verify(emailNotificationFacade, never()).notifyAdminForJobEyca(any(), anyInt(), anyInt(), anyInt());
        verify(emailNotificationFacade, never()).notifyEycaAdmin(anyList(), anyList(), anyList());
    }

    @Test
    void sendDiscountsToEyca_processingFailureNotifiesFailureAfterCommit() {
        when(configProperties.getEycaExportEnabled()).thenReturn(true);
        when(eycaDataExportRepository.findAll()).thenThrow(new IllegalStateException("read failed"));

        exportService.sendDiscountsToEyca();

        var inOrder = inOrder(transactionManager, emailNotificationFacade);
        inOrder.verify(transactionManager).commit(any(TransactionStatus.class));
        inOrder.verify(emailNotificationFacade).notifyAdminForJobEycaFailure(anyString());
        verify(emailNotificationFacade, never()).notifyAdminForJobEyca(any(), anyInt(), anyInt(), anyInt());
    }

    private static List<EycaDataExportViewEntity> staticCodeCreateRow() {
        EycaDataExportViewEntity row = TestUtils.getEycaDataExportForCreate().getFirst();
        row.setDiscountType(DiscountCodeTypeEnum.STATIC.getEycaDataCode());
        row.setStaticCode("TEST-CODE");
        row.setEycaEmailUpdateRequired(false);
        return List.of(row);
    }
}
