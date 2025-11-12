package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationEventEnum;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.email.EmailNotificationService;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.NotificationEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.*;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles({"dev"})
class CleanDiscountsBucketCodesJobTest
        extends IntegrationAbstractTest {

    @Autowired
    private CleanDiscountsBucketCodesJob job;

    @Autowired
    private ConfigProperties configProperties;

    private String trackingKey;
    private Instant cutoff;

    @SpyBean
    DiscountBucketCodeRepository discountBucketCodeRepositorySpy;

    @SpyBean
    private EmailNotificationService emailNotificationService;

    //@BeforeTransaction
    @BeforeEach
    void before() throws IOException {
        setAdminAuth();
        init();
    }

    @Test
    void Execute_ExecuteJob_NoBucketToDelete_JobNotRun()
            throws IOException {
        Assertions.assertDoesNotThrow(() -> job.execute(null));
        NotificationEntity ne = notificationRepository.findByKey(trackingKey);
        Assertions.assertNull(ne);
    }

    @Test
    void shouldPersistNotificationWithExpectedTrackingKey_whenJobRuns()
            throws IOException {
        DiscountBucketCodeRepository.CutoffInfo ci = mock(DiscountBucketCodeRepository.CutoffInfo.class);
        when(ci.getRetentionPeriod()).thenReturn("P6M");
        when(ci.getCutoff()).thenReturn(cutoff);
        doReturn(ci).when(discountBucketCodeRepositorySpy).computeCutoff();

        Mockito.clearInvocations(emailNotificationService);

        ArgumentCaptor<String> trackingKeyCaptor = ArgumentCaptor.forClass(String.class);

        job.execute(null);

        // assert (aspetta che l’async finisca e poi verifica/cattura)
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            ArgumentCaptor<String> tkCaptor = ArgumentCaptor.forClass(String.class);

            verify(emailNotificationService, atLeastOnce())
                    .trackNotification(
                            tkCaptor.capture(),
                            ArgumentMatchers.<String>isNull(),   // 2° arg può essere null
                            anyString()
                    );

            String capturedTrackingKey = tkCaptor.getAllValues().stream()
                                                 .filter(k -> k != null && k.startsWith("CLEAN_DISCOUNTS_BUCKET_CODES"))
                                                 .findFirst()
                                                 .orElseThrow(() -> new AssertionError("Clean job key not found"));
            NotificationEntity ne = notificationRepository.findByKey(capturedTrackingKey);
            assertNotNull(ne);
        });
    }

    private void init()
            throws IOException {

        clearNotification();

        trackingKey = EmailNotificationFacade.createTrackingKeyForEmailEventNotification(EmailNotificationEventEnum.CLEAN_DISCOUNTS_BUCKET_CODES);

        AgreementTestObject testObject = createApprovedAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        DiscountEntity discountEntity = testObject.getDiscountEntityList().get(0);

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.PUBLISHED);
        discountEntity.setEndDate(LocalDate.now().plusDays(3));
        discountEntity.setLastBucketCodeLoadFileName("codes.csv");
        discountEntity = discountRepository.save(discountEntity);

//        OffsetDateTime beforeCutoff = OffsetDateTime.parse("2024-01-10T10:00:00Z");
//        OffsetDateTime afterCutoff  = OffsetDateTime.parse("2025-01-10T10:00:00Z");
//        cutoff  = Instant.parse("2024-06-01T00:00:00Z");

        OffsetDateTime nowUtc   = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime cutoffOd = nowUtc.minusMonths(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
        cutoff       = cutoffOd.toInstant();
        OffsetDateTime beforeCutoff = cutoffOd.minusDays(1);
        OffsetDateTime afterCutoff  = cutoffOd.plusDays(1);


        DiscountEntity deBeforeCutoff = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        DiscountEntity deAfterCutoff = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);

        discountRepository.saveAll(List.of(deBeforeCutoff, deAfterCutoff));

        // creo i record di test
        DiscountBucketCodeEntity toDelete      = newCode(true, deBeforeCutoff, beforeCutoff);
        DiscountBucketCodeEntity shouldRemainBefore = newCode(false,deBeforeCutoff, beforeCutoff);
        DiscountBucketCodeEntity shouldRemainAfter = newCode(true, deAfterCutoff, afterCutoff);


        discountBucketCodeRepository.saveAll(List.of(toDelete, shouldRemainAfter, shouldRemainBefore));
        discountBucketCodeRepository.flush();

    }

    private DiscountBucketCodeEntity newCode(boolean used, DiscountEntity de, OffsetDateTime usageDatetime) {
        DiscountBucketCodeEntity e = new DiscountBucketCodeEntity();
        e.setIsUsed(used);
        e.setUsageDatetime(usageDatetime);
        e.setCode("x");
        e.setBucketCodeLoadId(0L);
        e.setDiscount(de);
        return e;
    }

    private void clearNotification() {
        notificationRepository.deleteAll();
    }
}
