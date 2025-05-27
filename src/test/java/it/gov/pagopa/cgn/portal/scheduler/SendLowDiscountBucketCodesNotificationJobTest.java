package it.gov.pagopa.cgn.portal.scheduler;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles({"dev"})
class SendLowDiscountBucketCodesNotificationJobTest
        extends IntegrationAbstractTest {

    @Autowired
    private SendLowDiscountBucketCodesNotificationJob job;

    @Autowired
    private AzureStorage azureStorage;

    @Autowired
    private ConfigProperties configProperties;

    private DiscountEntity discountEntity;


    @Test
    void Execute_ExecuteJob_NoBucketCodeSummaries_JobNotRun() {
        Assertions.assertDoesNotThrow(() -> job.execute(null));
    }

    @Test
    void Execute_ExecuteJob_NoBurnedCodes_NotificationNotSent()
            throws IOException {
        init();

        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_50, false);
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_25, false);
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_10, false);
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_0, false);

    }

    @Test
    void Execute_ExecuteJob_SendPercent50Notification()
            throws IOException {
        init();
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_50);
    }

    @Test
    void Execute_ExecuteJob_SendPercent25Notification()
            throws IOException {
        init();
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_25);
    }

    @Test
    void Execute_ExecuteJob_SendPercent10Notification()
            throws IOException {
        init();
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_10);
    }

    @Test
    void Execute_ExecuteJob_SendPercent0Notification()
            throws IOException {
        init();
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_0);
    }

    private void init()
            throws IOException {
        setAdminAuth();

        clearNotification();

        AgreementTestObject testObject = createApprovedAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        discountEntity = testObject.getDiscountEntityList().getFirst();

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        discountEntity.setEndDate(LocalDate.now().plusDays(3));
        discountEntity.setLastBucketCodeLoadFileName("codes.csv");
        discountRepository.save(discountEntity);

        BlobContainerClient documentContainerClient = new BlobContainerClientBuilder().connectionString(
                getAzureConnectionString()).containerName(configProperties.getDocumentsContainerName()).buildClient();
        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }

        // simulate bucket summary
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        DiscountBucketCodeSummaryEntity summary = discountBucketCodeSummaryRepository.findByDiscount(discountEntity);
        summary.setTotalCodes(10L);
        summary.setAvailableCodes(10L);
        discountBucketCodeSummaryRepository.save(summary);
    }

    private void clearNotification() {
        notificationRepository.deleteAll();
    }

    private void testNotification(BucketCodeExpiringThresholdEnum threshold) {
        testNotification(threshold, true);
    }

    private void testNotification(BucketCodeExpiringThresholdEnum threshold, boolean burnCodes) {
        if (burnCodes) {
            burnSummaryAvailableCodesToLeaveLessThanThresholdCodes(10, threshold, discountEntity);
        }

        job.execute(null);

        if (burnCodes) {
            Awaitility.await()
                      .atMost(10, TimeUnit.SECONDS)
                      .until(() -> notificationRepository.findByKey(EmailNotificationFacade.createTrackingKeyForExpirationNotification(
                              discountEntity,
                              threshold))!=null);
        } else {
            Awaitility.await()
                      .pollDelay(5, TimeUnit.SECONDS)
                      .until(() -> notificationRepository.findByKey(EmailNotificationFacade.createTrackingKeyForExpirationNotification(
                              discountEntity,
                              threshold))==null);
        }
    }
}
