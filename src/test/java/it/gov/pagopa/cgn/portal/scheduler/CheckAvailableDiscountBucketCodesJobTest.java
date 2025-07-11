package it.gov.pagopa.cgn.portal.scheduler;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
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
class CheckAvailableDiscountBucketCodesJobTest
        extends IntegrationAbstractTest {

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
    void Execute_ExecuteJob_UpdateAvailableCodes()
            throws IOException {
        init();
        updateSummary();
    }

    private void init()
            throws IOException {
        setAdminAuth();

        AgreementTestObject testObject = createApprovedAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        discountEntity = testObject.getDiscountEntityList().get(0);

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        discountEntity.setEndDate(LocalDate.now().plusDays(3));
        discountEntity.setLastBucketCodeLoadFileName("codes.csv");
        discountRepository.save(discountEntity);

        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));
        MockMultipartFile multipartFile = new MockMultipartFile("bucketload", "test-codes.csv", "text/csv", csv);

        BlobContainerClient documentContainerClient = new BlobContainerClientBuilder().connectionString(
                getAzureConnectionString()).containerName(configProperties.getDocumentsContainerName()).buildClient();
        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }

        bucketService.prepareDiscountBucketCodeSummary(discountEntity);

        // load 10 codes by uploading 5 times a "2 code" bucket.
        for (var i = 0; i < 5; i++) {
            var bucketCodeLoadUid = TestUtils.generateDiscountBucketCodeUid();

            azureStorage.uploadCsv(multipartFile.getBytes(), bucketCodeLoadUid, multipartFile.getSize());

            discountEntity.setLastBucketCodeLoadUid(bucketCodeLoadUid);
            discountRepository.save(discountEntity);

            bucketService.createPendingBucketLoad(discountEntity);
            bucketService.setRunningBucketLoad(discountEntity.getId());
            bucketService.performBucketLoad(discountEntity.getId());
        }
    }

    private void updateSummary() {
        burnBucketCodesToLeaveLessThanThresholdCodes(10, BucketCodeExpiringThresholdEnum.PERCENT_50, discountEntity);

        job.execute(null);

        Awaitility.await()
                  .atMost(15, TimeUnit.SECONDS)
                  .until(() -> discountBucketCodeSummaryRepository.findByDiscount(discountEntity).getAvailableCodes()==
                               5L);
    }
}
