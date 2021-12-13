package it.gov.pagopa.cgn.portal.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.BucketCodeLoadEntity;
import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.BucketCodeLoadRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.util.BucketLoadUtils;

@SpringBootTest
@ActiveProfiles("dev")
class BucketServiceTest extends IntegrationAbstractTest {

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private AzureStorage azureStorage;

    @Autowired
    private BucketService bucketService;

    @Autowired
    private BucketCodeLoadRepository bucketCodeLoadRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountBucketCodeRepository discountBucketCodeRepository;

    @Autowired
    private BucketLoadUtils bucketLoadUtils;

    private AgreementEntity agreementEntity;
    private MockMultipartFile multipartFile;

    @BeforeEach
    void init() throws IOException {
        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));
        multipartFile = new MockMultipartFile("bucketload", "test-codes.csv", "text/csv", csv);

        BlobContainerClient documentContainerClient = new BlobContainerClientBuilder()
                .connectionString(getAzureConnectionString())
                .containerName(configProperties.getDocumentsContainerName()).buildClient();
        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }
    }

    @Test
    void Create_CreatePendingBucketCodeLoad_Ok() {

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountEntity.setId(1L);

        bucketService.createPendingBucketLoad(discountEntity);
        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
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

        azureStorage.uploadCsv(multipartFile.getInputStream(), discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());

        Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoad().getUid()));

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository
                .findById(discountEntity.getLastBucketCodeLoad().getId()).orElseThrow();

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
        bucketService.setRunningBucketLoad(discountEntity.getId());

        bucketService.performBucketLoad(discountEntity.getId());
        Assertions.assertFalse(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoad().getUid() + ".csv"));

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
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

        azureStorage.uploadCsv(multipartFile.getInputStream(), discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoadUid()));
        discountEntity = bucketService.createPendingBucketLoad(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());
        bucketService.performBucketLoad(discountEntity.getId());

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FINISHED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertEquals(2, bucketCodeLoadEntity.getNumberOfCodes());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

        List<DiscountBucketCodeEntity> codes = discountBucketCodeRepository.findAllByDiscount(discountEntity);
        Assertions.assertFalse(codes.isEmpty());
    }

    @Test
    void Async_PerformBucketCodeStore_Ok() throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getInputStream(), discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        Assertions.assertTrue(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoadUid() + ".csv"));
        bucketService.createPendingBucketLoad(discountEntity);
        bucketLoadUtils.storeCodesBucket(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 2);

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
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

        azureStorage.uploadCsv(multipartFile.getInputStream(), discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoadUid()));
        discountEntity = bucketService.createPendingBucketLoad(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());
        bucketService.performBucketLoad(discountEntity.getId());

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
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

        azureStorage.uploadCsv(multipartFile.getInputStream(), discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        Assertions.assertTrue(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoadUid() + ".csv"));
        bucketService.createPendingBucketLoad(discountEntity);
        bucketLoadUtils.storeCodesBucket(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 2);

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

        bucketLoadUtils.deleteBucketCodes(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 0);
    }
}
