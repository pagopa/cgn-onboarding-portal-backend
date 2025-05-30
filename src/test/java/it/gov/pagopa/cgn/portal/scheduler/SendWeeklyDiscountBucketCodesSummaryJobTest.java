package it.gov.pagopa.cgn.portal.scheduler;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles({"dev"})
public class SendWeeklyDiscountBucketCodesSummaryJobTest
        extends IntegrationAbstractTest {

    private final SendWeeklyDiscountBucketCodesSummaryJob job;

    private final AzureStorage azureStorage;

    private final ConfigProperties configProperties;


    @Autowired
    public SendWeeklyDiscountBucketCodesSummaryJobTest(SendWeeklyDiscountBucketCodesSummaryJob job,
                                                       AzureStorage azureStorage,
                                                       ConfigProperties configProperties) {
        this.job = job;
        this.azureStorage = azureStorage;
        this.configProperties = configProperties;
    }

    @BeforeEach
    public void init()
            throws IOException {
        setAdminAuth();

        List<AgreementTestObject> testObjectList = createMultipleApprovedAgreement(5);

        for (AgreementTestObject testObject : testObjectList) {

            AgreementEntity agreementEntity = testObject.getAgreementEntity();
            List<DiscountEntity> discountEntityList = testObject.getDiscountEntityList();

            for (DiscountEntity discountEntity : discountEntityList) {

                // simulate test passed
                discountEntity.setState(DiscountStateEnum.TEST_PASSED);
                discountEntity = discountRepository.save(discountEntity);

                discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
                discountEntity.setEndDate(LocalDate.now().plusDays(3));
                discountEntity.setLastBucketCodeLoadFileName("codes.csv");
                discountRepository.save(discountEntity);

                byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));
                MockMultipartFile multipartFile = new MockMultipartFile("bucketload",
                                                                        "test-codes.csv",
                                                                        "text/csv",
                                                                        csv);

                BlobContainerClient documentContainerClient = new BlobContainerClientBuilder().connectionString(
                                                                                                      getAzureConnectionString())
                                                                                              .containerName(
                                                                                                      configProperties.getDocumentsContainerName())
                                                                                              .buildClient();
                if (!documentContainerClient.exists()) {
                    documentContainerClient.create();
                }

                bucketService.prepareDiscountBucketCodeSummary(discountEntity);

                int bucketSize = 2;
                int minTotal = 10;
                int maxTotal = 50;

                // Generate a random total between 100 and 500, rounded to the nearest multiple of bucketSize
                int totalCodes = (new Random().nextInt((maxTotal - minTotal) / bucketSize + 1) * bucketSize) + minTotal;

                // Calculate how many buckets we need to upload
                int iterations = totalCodes / bucketSize;

                for (var i = 0; i < iterations; i++) {
                    var bucketCodeLoadUid = TestUtils.generateDiscountBucketCodeUid();

                    azureStorage.uploadCsv(multipartFile.getBytes(), bucketCodeLoadUid, multipartFile.getSize());

                    discountEntity.setLastBucketCodeLoadUid(bucketCodeLoadUid);
                    discountRepository.save(discountEntity);

                    bucketService.createPendingBucketLoad(discountEntity);
                    bucketService.setRunningBucketLoad(discountEntity.getId());
                    bucketService.performBucketLoad(discountEntity.getId());
                }
            }
        }
    }

    @Test
    public void testNotifications()
            throws IOException {

        job.execute(null);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(this::verifySavedNotifications);
    }

    public boolean verifySavedNotifications() {
        try {

            List<DiscountBucketCodeSummaryEntity> summaries = discountBucketCodeSummaryRepository.findAllPublishedNotExpired();
            discountBucketCodeSummaryRepository.findAllPublishedNotExpired().forEach(summary -> {
                DiscountEntity d = discountRepository.findById(summary.getId()).orElseThrow();
                AgreementEntity agreement = d.getAgreement();
                ProfileEntity p = profileRepository.findByAgreementId(agreement.getId()).orElseThrow();
                notificationRepository.findByKey(EmailNotificationFacade.createTrackingKeyForWeeklySummaryNotification(p));
            });

        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
