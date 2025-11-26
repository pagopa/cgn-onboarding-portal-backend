package it.gov.pagopa.cgn.portal.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.BucketCodeLoadRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeSummaryRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.util.BucketLoadUtils;
import it.gov.pagopa.cgn.portal.util.CsvUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("dev")
class BucketServiceTest
        extends IntegrationAbstractTest {

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

    private AgreementEntity agreementEntity;
    private MockMultipartFile multipartFile;

    @BeforeEach
    void init()
            throws IOException {
        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                      EntityType.PRIVATE,
                                                                      TestUtils.FAKE_ORGANIZATION_NAME);
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
        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                                                                                                    .getId()).get();
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
    void Create_SetRunningBucketCodeLoad_Ok()
            throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                               discountEntity.getLastBucketCodeLoadUid(),
                               multipartFile.getSize());

        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketService.setRunningBucketLoad(discountEntity.getId());

        Assertions.assertTrue(bucketService.checkBucketLoadUID(discountEntity.getLastBucketCodeLoad().getUid()));

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                                                                                                    .getId())
                                                                            .orElseThrow();

        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.RUNNING, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.RUNNING.getCode(), bucketCodeLoadEntity.getStatus().getCode());
        Assertions.assertEquals(2, bucketCodeLoadEntity.getNumberOfCodes()); // mocked files has 2 codes
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
    }

    @Test
    void PerformBucketCodeStore_Ko() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);

        // we do not set the bucket load running to force an exception inside performBucketLoad
        bucketService.performBucketLoad(discountEntity.getId());
        Assertions.assertFalse(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoad().getUid() + ".csv"));

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                                                                                                    .getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FAILED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FAILED.getCode(), bucketCodeLoadEntity.getStatus().getCode());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());
    }

    @Test
    void PerformBucketCodeStore_Ok()
            throws IOException {
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

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                                                                                                    .getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FINISHED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertEquals(2, bucketCodeLoadEntity.getNumberOfCodes());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

        DiscountBucketCodeSummaryEntity discountBucketCodeSummaryEntity = discountBucketCodeSummaryRepository.findByDiscount(
                discountEntity);
        Assertions.assertEquals(2, discountBucketCodeSummaryEntity.getTotalCodes());
        Assertions.assertEquals(2, discountBucketCodeSummaryEntity.getAvailableCodes());

        List<DiscountBucketCodeEntity> codes = discountBucketCodeRepository.findAllByDiscount(discountEntity);
        Assertions.assertFalse(codes.isEmpty());
        codes.forEach(codeEntity -> {
            Assertions.assertFalse((boolean) codeEntity.getIsUsed());
            Assertions.assertNull(codeEntity.getUsageDatetime());
        });
    }

    @Test
    void Async_PerformBucketCodeStore_Ok()
            throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                               discountEntity.getLastBucketCodeLoadUid(),
                               multipartFile.getSize());

        Assertions.assertTrue(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoadUid() + ".csv"));
        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketLoadUtils.storeCodesBucket(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count()==2);
        Awaitility.await()
                  .atMost(5, TimeUnit.SECONDS)
                  .until(() -> bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId())
                                                       .get()
                                                       .getStatus()==BucketCodeLoadStatusEnum.FINISHED);

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                                                                                                    .getId()).get();
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
    void PerformBucketCodeDelete_Ok()
            throws IOException {
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

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                                                                                                    .getId()).get();
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
    void Async_PerformBucketCodeDelete_Ok()
            throws IOException {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountRepository.save(discountEntity);

        azureStorage.uploadCsv(multipartFile.getBytes(),
                               discountEntity.getLastBucketCodeLoadUid(),
                               multipartFile.getSize());

        Assertions.assertTrue(azureStorage.existsDocument(discountEntity.getLastBucketCodeLoadUid() + ".csv"));
        bucketService.createPendingBucketLoad(discountEntity);
        bucketService.prepareDiscountBucketCodeSummary(discountEntity);
        bucketLoadUtils.storeCodesBucket(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count()==2);
        Awaitility.await()
                  .atMost(5, TimeUnit.SECONDS)
                  .until(() -> bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad().getId())
                                                       .get()
                                                       .getStatus()==BucketCodeLoadStatusEnum.FINISHED);

        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                                                                                                    .getId()).get();
        Assertions.assertNotNull(bucketCodeLoadEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), bucketCodeLoadEntity.getDiscountId());
        Assertions.assertEquals(BucketCodeLoadStatusEnum.FINISHED, bucketCodeLoadEntity.getStatus());
        Assertions.assertEquals(discountEntity.getLastBucketCodeLoad().getId(), bucketCodeLoadEntity.getId());
        Assertions.assertNotNull(bucketCodeLoadEntity.getFileName());

        bucketLoadUtils.deleteBucketCodes(discountEntity.getId());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count()==0);
    }

    @Test
    @Disabled("local test")
    void checkDiscountBucket_loadingCsvBucket()
            throws IOException {

        String path = "c:\\develop\\test-buckets\\";
        String fileName = "ALL-KO-data-1739964881841";
        String ext = ".csv";
        try (InputStream inputStream = new FileInputStream(path + fileName + ext)) {
            byte[] content = inputStream.readAllBytes();
            long csvRecordCount = countCsvRecord(content);
            if (csvRecordCount < 10000) {
                throw new InvalidRequestException(ErrorCodeEnum.CANNOT_LOAD_BUCKET_FOR_NOT_RESPECTED_MINIMUM_BOUND.getValue());
            }
            try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
                Stream<CSVRecord> csvRecordStream = CsvUtils.getCsvRecordStream(contentIs);
                if (content.length==0) {
                    throw new InternalErrorException(ErrorCodeEnum.CSV_DATA_NOT_VALID.getValue());
                }
                AtomicInteger currentRow = new AtomicInteger(1);
                csvRecordStream.forEach(line -> {
                    if (line.get(0).length() > 20 || StringUtils.isBlank(line.get(0))) {
                        System.out.println(ErrorCodeEnum.MAX_ALLOWED_BUCKET_CODE_LENGTH_NOT_RESPECTED.getValue() + " " +
                                           currentRow.get() + " " + line.get(0));
                    }
                    currentRow.incrementAndGet();
                });
            }

            Pattern pDigits = Pattern.compile("\\d"); //[0-9]
            Pattern pAlphab = Pattern.compile("[A-Za-z]");
            Pattern spChars = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d-]{1,20}$"); //^(?=.*\d)[a-zA-Z0-9][-a-zA-Z0-9]+$

            try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
                Stream<CSVRecord> csvRecordStream = CsvUtils.getCsvRecordStream(contentIs);

                AtomicInteger invalidCodes = new AtomicInteger(0);
                AtomicInteger cursorRow = new AtomicInteger(0);
                csvRecordStream.forEach(line -> {
                    cursorRow.incrementAndGet();
                    if (line.get(0).length() > 20 || StringUtils.isBlank(line.get(0))) {
                        //System.out.println(ErrorCodeEnum.MAX_ALLOWED_BUCKET_CODE_LENGTH_NOT_RESPECTED.getValue()+" "+ cursorRow.get() + " " + line.get(0));
                        invalidCodes.incrementAndGet();
                        return;
                    }

                    if (!(pDigits.matcher(line.get(0)).find() //at least one digit
                          && pAlphab.matcher(line.get(0)).find())) { //at least on alphab. char)
                        //System.out.println(ErrorCodeEnum.BUCKET_CODES_MUST_BE_ALPHANUM_WITH_AT_LEAST_ONE_DIGIT_AND_ONE_CHAR.getValue()+ " "+ cursorRow.get() + " " + line.get(0));
                        invalidCodes.incrementAndGet();
                        return;
                    }

                    if (!(spChars.matcher(line.get(0)).find())) {
                        //System.out.println(ErrorCodeEnum.NOT_ALLOWED_SPECIAL_CHARS.getValue()+" "+ cursorRow.get() + " " + line.get(0));
                        invalidCodes.incrementAndGet();
                    }
                });
                System.out.println("Total codes:" + cursorRow.get());
                System.out.println("Total invalid codes:" + invalidCodes.get());
            }
            Assertions.assertTrue(csvRecordCount > 0);
        }
    }

    @Test
    void shouldReturnCutoffAndRetentionPeriodWhenComputingCutoff() {
        DiscountBucketCodeRepository.CutoffInfo info = discountBucketCodeRepository.computeCutoff();
        Assertions.assertNotNull(info.getCutoff());
        Assertions.assertNotNull(info.getRetentionPeriod());

    }

    @Transactional
    @Test
    void shouldDeleteOnlyUsedBucketCodesWithUsageDateBeforeCutoff() {
        // given
        OffsetDateTime beforeCutoff = OffsetDateTime.parse("2024-01-10T10:00:00Z");
        OffsetDateTime afterCutoff  = OffsetDateTime.parse("2025-01-10T10:00:00Z");
        Instant cutoff       = Instant.parse("2024-06-01T00:00:00Z");

        DiscountEntity deBeforeCutoff = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        DiscountEntity deAfterCutoff = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);

        discountRepository.saveAll(List.of(deBeforeCutoff,deAfterCutoff));

        // creo i record di test
        DiscountBucketCodeEntity toDelete      = newCode(true, deBeforeCutoff, beforeCutoff);
        DiscountBucketCodeEntity shouldRemainBefore = newCode(false,deBeforeCutoff, beforeCutoff);
        DiscountBucketCodeEntity shouldRemainAfter = newCode(true, deAfterCutoff, afterCutoff);


        discountBucketCodeRepository.saveAll(List.of(toDelete, shouldRemainAfter, shouldRemainBefore));
        discountBucketCodeRepository.flush();

        // when
        long deletedCount = discountBucketCodeRepository.deleteAllBucketCodesUsedBeforeCutoff(cutoff);

        // then
        Assertions.assertEquals(1L, deletedCount);

        List<DiscountBucketCodeEntity> remaining = discountBucketCodeRepository.findAll();
        Assertions.assertEquals(2, remaining.size());

        // verifica che rimanga 1 usato (quello dopo cutoff) e 1 non usato
        long usedCount = remaining.stream().filter(DiscountBucketCodeEntity::getIsUsed).count();
        Assertions.assertEquals(1L, usedCount);

        boolean existsUsedAfterCutoff = remaining.stream()
                                                 .anyMatch(c -> c.getIsUsed() && !c.getUsageDatetime().toInstant().isBefore(cutoff));
        Assertions.assertTrue(existsUsedAfterCutoff);

        boolean existsNotUsed = remaining.stream()
                                         .anyMatch(c -> !c.getIsUsed());
        Assertions.assertTrue(existsNotUsed);
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

    private long countCsvRecord(byte[] content) {
        long recordCount = 0;
        try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
            recordCount = CsvUtils.countCsvLines(contentIs);
        } catch (IOException e) {
            throw new CGNException(e.getMessage());
        }
        return recordCount;
    }
}
