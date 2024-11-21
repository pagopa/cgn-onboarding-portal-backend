package it.gov.pagopa.cgn.portal.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.*;
import it.gov.pagopa.cgn.portal.util.BucketLoadUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("dev")
class BucketServiceTest extends IntegrationAbstractTest {

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private AzureStorage azureStorage;

    @Autowired
    private BucketCodeLoadRepository bucketCodeLoadRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountBucketCodeRepository discountBucketCodeRepository;

    @Autowired
    private DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository;

    @Autowired
    private BucketLoadUtils bucketLoadUtils;

    @Autowired
    private BucketService bucketService;

    @Autowired
    private NotificationRepository notificationRepository;

    private AgreementEntity agreementEntity;
    private MockMultipartFile multipartFile;

    @BeforeEach
    void init() throws IOException {
        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE,TestUtils.FAKE_ORGANIZATION_NAME);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));
        multipartFile = new MockMultipartFile("bucketload", "test-codes.csv", "text/csv", csv);

        BlobContainerClient documentContainerClient = new BlobContainerClientBuilder().connectionString(
                getAzureConnectionString()).containerName(configProperties.getDocumentsContainerName()).buildClient();
        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }
    }

    @Test
    void Create_CreatePendingBucketCodeLoad_Ok() {

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountEntity.setId(1L);

        bucketService.createPendingBucketLoad(discountEntity);
        BucketCodeLoadEntity bucketCodeLoadEntity
                = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.PENDING, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertNull(bucketCodeLoadEntity.getNumberOfCodes());
        Assertions.assertEquals(bucketCodeLoadEntity.hashCode(), bucketCodeLoadEntity.hashCode());
        Assertions.assertEquals(bucketCodeLoadEntity.toString(), bucketCodeLoadEntity.toString());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

    }

    @Test
    void Create_SetRunningBucketCodeLoad_Ok() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());

        Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoad().getUid()));

        BucketCodeLoadEntity bucketCodeLoadEntity
                = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).orElseThrow();

        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.RUNNING, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.RUNNING.getCode(), bucketCodeLoadEntity.getStatus().getCode());
        Assertions.assertEquals(2, bucketCodeLoadEntity.getNumberOfCodes()); // mocked files has 2 codes
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertEquals(bucketCodeLoadEntity.getFileName(), bucketCodeLoadEntity.getFileName());
    }

    @Test
    void PerformBucketCodeStore_Ko() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        // we do not set the bucket load running to force an exception inside performBucketLoad
        //bucketService.setRunningBucketLoad(discountEntity.getId());

        bucketService.performBucketLoad(discountEntity.getId());
        Assertions.assertFalse(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoad().getUid() + ".csv"));

        BucketCodeLoadEntity bucketCodeLoadEntity
                = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FAILED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FAILED.getCode(), bucketCodeLoadEntity.getStatus().getCode());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());
    }

    @Test
    void PerformBucketCodeStore_Ok() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoadUid()));
        discountEntity = bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());
        bucketService.performBucketLoad(discountEntity.getId());

        BucketCodeLoadEntity bucketCodeLoadEntity
                = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FINISHED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertEquals(2, bucketCodeLoadEntity.getNumberOfCodes());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

        DiscountBucketCodeSummaryEntity discountBucketCodeSummaryEntity
                = discountBucketCodeSummaryRepository.findByDiscount(discountEntity);
        Assertions.assertEquals(2, discountBucketCodeSummaryEntity.getAvailableCodes());

        List<DiscountBucketCodeEntity> codes = discountBucketCodeRepository.findAllByDiscount(discountEntity);
        Assertions.assertFalse(codes.isEmpty());
    }

    @Test
    void Async_PerformBucketCodeStore_Ok() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        Assertions.assertTrue(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoadUid() + ".csv"));
        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketLoadUtils.storeCodesBucket(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 2);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId())
                .get()
                .getStatus() == BucketCodeLoadStatusEnum.FINISHED);

        BucketCodeLoadEntity bucketCodeLoadEntity
                = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FINISHED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertEquals(2, bucketCodeLoadEntity.getNumberOfCodes());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());
    }

    @Test
    void BucketCodeLoadData_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        BucketCodeLoadEntity bucketCodeLoadEntity = new BucketCodeLoadEntity();
        bucketCodeLoadEntity.setDiscountId(discountEntity.getId());
        bucketCodeLoadEntity.setUid(discountEntity.getLastBucketCodeLoadUid());
        bucketCodeLoadEntity.setFileName(discountEntity.getLastBucketCodeLoadFileName());
        bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.PENDING);
        bucketCodeLoadEntity.setNumberOfCodes(100L);

        BucketCodeLoadEntity inserted = bucketCodeLoadRepository.save(bucketCodeLoadEntity);

        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.PENDING, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoadUid(), bucketCodeLoadEntity.getUid());
        Assertions.assertEquals(bucketCodeLoadEntity.hashCode(), bucketCodeLoadEntity.hashCode());
        Assertions.assertEquals(bucketCodeLoadEntity.toString(), bucketCodeLoadEntity.toString());
        Assertions.assertEquals(bucketCodeLoadEntity.getNumberOfCodes(), inserted.getNumberOfCodes());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

    }

    @Test
    void PerformBucketCodeDelete_Ok() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoadUid()));
        discountEntity = bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());
        bucketService.performBucketLoad(discountEntity.getId());

        BucketCodeLoadEntity bucketCodeLoadEntity
                = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FINISHED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertEquals(2, bucketCodeLoadEntity.getNumberOfCodes());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

        List<DiscountBucketCodeEntity> codes = discountBucketCodeRepository.findAllByDiscount(discountEntity);
        Assertions.assertFalse(codes.isEmpty());
        Assertions.assertEquals(2, (long) codes.size());

        bucketService.deleteBucketCodes(discountEntity.getId());

        codes = discountBucketCodeRepository.findAllByDiscount(discountEntity);
        Assertions.assertTrue(codes.isEmpty());
    }

    @Test
    void Async_PerformBucketCodeDelete_Ok() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        Assertions.assertTrue(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoadUid() + ".csv"));
        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketLoadUtils.storeCodesBucket(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 2);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId())
                .get()
                .getStatus() == BucketCodeLoadStatusEnum.FINISHED);

        BucketCodeLoadEntity bucketCodeLoadEntity
                = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FINISHED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

        bucketLoadUtils.deleteBucketCodes(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 0);
    }

    @Test
    void CheckDiscountBucketCodeSummaryExpirationAndSendNotification_BucketNotLoaded_NoNotifications() throws
            IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());
        // we do not run loading => bucket code summary available codes are 0
        // => job should skip the check and not require a notification

        var discountBucketCodeSummaryEntity = discountBucketCodeSummaryRepository.findByDiscount(discountEntity);
        Assertions.assertEquals(0, discountBucketCodeSummaryEntity.getAvailableCodes());

        var notificationRequired = bucketService.checkDiscountBucketCodeSummaryExpirationAndSendNotification(
                discountBucketCodeSummaryEntity);
        // no notification should be sent because bucket has not been loaded yet
        Assertions.assertFalse(notificationRequired);
    }

    @Test
    void CheckDiscountBucketCodeSummaryExpirationAndSendNotification_NotificationNotSent() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());
        bucketService.performBucketLoad(discountEntity.getId());

        Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoad().getUid()));

        var discountBucketCodeSummaryEntity = discountBucketCodeSummaryRepository.findByDiscount(discountEntity);
        var notificationRequired = bucketService.checkDiscountBucketCodeSummaryExpirationAndSendNotification(
                discountBucketCodeSummaryEntity);

        // no notification should be sent because all codes are available
        Assertions.assertFalse(notificationRequired);
    }

    @Test
    void CheckDiscountBucketCodeSummaryExpirationAndSendNotification_Percent50notificationRequired() throws
            IOException {
        var discountEntity = setupDiscount();
        var notificationRequired = testNotification(discountEntity, BucketCodeExpiringThresholdEnum.PERCENT_50);
        Assertions.assertTrue(notificationRequired);
    }

    @Test
    void CheckDiscountBucketCodeSummaryExpirationAndSendNotification_Percent25notificationRequired() throws
            IOException {
        var discountEntity = setupDiscount();
        var notificationRequired = testNotification(discountEntity, BucketCodeExpiringThresholdEnum.PERCENT_25);
        Assertions.assertTrue(notificationRequired);
    }

    @Test
    void CheckDiscountBucketCodeSummaryExpirationAndSendNotification_Percent10notificationRequired() throws
            IOException {
        var discountEntity = setupDiscount();
        var notificationRequired = testNotification(discountEntity, BucketCodeExpiringThresholdEnum.PERCENT_10);
        Assertions.assertTrue(notificationRequired);
    }

    @Test
    void CheckDiscountBucketCodeSummaryExpirationAndSendNotification_Percent0notificationRequired() throws IOException {
        var discountEntity = setupDiscount();
        var notificationRequired = testNotification(discountEntity, BucketCodeExpiringThresholdEnum.PERCENT_0);
        Assertions.assertTrue(notificationRequired);
        var discountBucketCodeSummaryEntity = discountBucketCodeSummaryRepository.findByDiscount(discountEntity);
        Assertions.assertNotNull(discountBucketCodeSummaryEntity.getExpiredAt());
    }

    @Test
    void CheckDiscountBucketCodeSummaryExpirationAndSendNotification_NoDoubleNotification() throws IOException {
        var discountEntity = setupDiscount();

        var notificationRequired = testNotification(discountEntity, BucketCodeExpiringThresholdEnum.PERCENT_0);
        Assertions.assertTrue(notificationRequired);
        var firstNotification
                = notificationRepository.findByKey(EmailNotificationFacade.createTrackingKeyForExpirationNotification(
                        discountEntity,
                        BucketCodeExpiringThresholdEnum.PERCENT_0));

        notificationRequired = testNotification(discountEntity, BucketCodeExpiringThresholdEnum.PERCENT_0);
        Assertions.assertTrue(notificationRequired);
        var secondNotification
                = notificationRepository.findByKey(EmailNotificationFacade.createTrackingKeyForExpirationNotification(
                        discountEntity,
                        BucketCodeExpiringThresholdEnum.PERCENT_0));

        Assertions.assertNotNull(firstNotification);
        Assertions.assertNotNull(secondNotification);
        Assertions.assertEquals(firstNotification, secondNotification);
    }

    private DiscountEntity setupDiscount() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);
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

            Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoad().getUid()));
        }

        return discountEntity;
    }

    private boolean testNotification(DiscountEntity discountEntity, BucketCodeExpiringThresholdEnum threshold) {
        var discountBucketCodeSummaryEntity = discountBucketCodeSummaryRepository.findByDiscount(discountEntity);
        Assertions.assertEquals(10, discountBucketCodeSummaryEntity.getAvailableCodes());

        burnBucketCodesToLeaveLessThanThresholdCodes(threshold, discountEntity);

        var notificationRequired = bucketService.checkDiscountBucketCodeSummaryExpirationAndSendNotification(
                discountBucketCodeSummaryEntity);

        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> notificationRepository.findByKey(EmailNotificationFacade.createTrackingKeyForExpirationNotification(discountEntity, threshold)) != null);

        return notificationRequired;
    }
}
