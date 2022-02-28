package it.gov.pagopa.cgn.portal.scheduler;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
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
class CheckAvailableDiscountBucketCodesJobTest extends IntegrationAbstractTest {

    @Autowired
    private CheckAvailableDiscountBucketCodesJob job;

    @Autowired
    private AzureStorage azureStorage;

    @Autowired
    private ConfigProperties configProperties;

    private DiscountEntity discountEntity;

    @Test
    void Execute_ExecuteJob_NoBucketCodeSummaries() {
        Assertions.assertDoesNotThrow(() -> job.execute(null));
    }

    @Test
    void Execute_ExecuteJob_SendPercent50Notification() throws IOException {
        init();
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_50);
    }

    @Test
    void Execute_ExecuteJob_SendPercent25Notification() throws IOException {
        init();
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_25);
    }

    @Test
    void Execute_ExecuteJob_SendPercent10Notification() throws IOException {
        init();
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_10);
    }

    @Test
    void Execute_ExecuteJob_SendPercent0Notification() throws IOException {
        init();
        testNotification(BucketCodeExpiringThresholdEnum.PERCENT_0);
    }

    private void init() throws IOException {
        setAdminAuth();

        AgreementTestObject testObject = createApprovedAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        discountEntity = testObject.getDiscountEntityList().get(0);
        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        discountEntity.setEndDate(LocalDate.now().plusDays(3));
        discountEntity.setLastBucketCodeLoadFileName("codes.csv");
        discountRepository.save(discountEntity);

        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));
        MockMultipartFile multipartFile = new MockMultipartFile("bucketload", "test-codes.csv", "text/csv", csv);

        BlobContainerClient documentContainerClient = new BlobContainerClientBuilder()
                .connectionString(getAzureConnectionString())
                .containerName(configProperties.getDocumentsContainerName()).buildClient();
        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }

        bucketService.prepareDiscountBucketCodeSummary(discountEntity);

        // load 10 codes by uploading 5 times a "2 code" bucket.
        for (var i = 0; i < 5; i++) {
            var bucketCodeLoadUid = TestUtils.generateDiscountBucketCodeUid();

            azureStorage.uploadCsv(multipartFile.getInputStream(), bucketCodeLoadUid, multipartFile.getSize());

            discountEntity.setLastBucketCodeLoadUid(bucketCodeLoadUid);
            discountRepository.save(discountEntity);

            bucketService.createPendingBucketLoad(discountEntity);
            bucketService.setRunningBucketLoad(discountEntity.getId());
            bucketService.performBucketLoad(discountEntity.getId());
        }
    }

    private void testNotification(BucketCodeExpiringThresholdEnum threshold) {
        burnBucketCodesToLeaveLessThanThresholdCodes(threshold, discountEntity);

        job.execute(null);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> notificationRepository.count() >= 1);

        var notification = notificationRepository.findByKey(EmailNotificationFacade.createTrackingKeyForExiprationNotification(discountEntity, threshold));
        Assertions.assertNotNull(notification);
    }
}
