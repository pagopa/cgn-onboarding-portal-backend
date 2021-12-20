package it.gov.pagopa.cgn.portal.controller.discount;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.discount.DiscountConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.BucketService;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgn.portal.util.BucketLoadUtils;
import it.gov.pagopa.cgnonboardingportal.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class DiscountApiTest extends IntegrationAbstractTest {

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private AzureStorage azureStorage;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private BucketLoadUtils bucketLoadUtils;

    @Autowired
    private BucketService bucketService;

    @Autowired
    private ObjectMapper objectMapper;

    private String discountPath;
    private AgreementEntity agreement;
    private final DiscountConverter discountConverter = new DiscountConverter();

    private MockMultipartFile multipartFile;

    void initTest(DiscountCodeTypeEnum discountCodeType) throws IOException {
        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreement, SalesChannelEnum.ONLINE,
                discountCodeType);
        profileService.createProfile(profileEntity, agreement.getId());
        discountPath = TestUtils.getDiscountPath(agreement.getId());
        setOperatorAuth();
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
    void Create_CreateDiscount_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);
        CreateDiscount discount = createSampleCreateDiscountWithStaticCode();
        this.mockMvc.perform(post(discountPath).contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(discount))).andDo(log()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue())) // default state
                .andExpect(jsonPath("$.name").value(discount.getName()))
                .andExpect(jsonPath("$.description").value(discount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(discount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(discount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(discount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").value(discount.getStaticCode()))
                .andExpect(jsonPath("$.landingPageUrl").value(discount.getLandingPageUrl()))
                .andExpect(jsonPath("$.landingPageReferrer").value(discount.getLandingPageReferrer()))
                .andExpect(jsonPath("$.condition").value(discount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.suspendedReasonMessage").isEmpty());
    }

    @Test
    void Create_CreateDiscountWithLandingPage_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.LANDINGPAGE);
        CreateDiscount discount = createSampleCreateDiscountWithLandingPage();
        this.mockMvc.perform(post(discountPath).contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(discount))).andDo(log()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue())) // default state
                .andExpect(jsonPath("$.name").value(discount.getName()))
                .andExpect(jsonPath("$.description").value(discount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(discount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(discount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(discount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").value(discount.getStaticCode()))
                .andExpect(jsonPath("$.landingPageUrl").value(discount.getLandingPageUrl()))
                .andExpect(jsonPath("$.landingPageReferrer").value(discount.getLandingPageReferrer()))
                .andExpect(jsonPath("$.condition").value(discount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.suspendedReasonMessage").isEmpty());
    }

    @Test
    void Create_CreateDiscountWithBucket_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.BUCKET);
        CreateDiscount discount = createSampleCreateDiscountWithBucket();
        azureStorage.uploadCsv(multipartFile.getInputStream(), discount.getLastBucketCodeLoadUid(),
                multipartFile.getSize());
        this.mockMvc.perform(post(discountPath).contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(discount))).andDo(log()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue())) // default state
                .andExpect(jsonPath("$.name").value(discount.getName()))
                .andExpect(jsonPath("$.description").value(discount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(discount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(discount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(discount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").value(discount.getStaticCode()))
                .andExpect(jsonPath("$.landingPageUrl").value(discount.getLandingPageUrl()))
                .andExpect(jsonPath("$.landingPageReferrer").value(discount.getLandingPageReferrer()))
                .andExpect(jsonPath("$.lastBucketCodeLoadUid").value(discount.getLastBucketCodeLoadUid()))
                .andExpect(jsonPath("$.lastBucketCodeLoadFileName").value(discount.getLastBucketCodeLoadFileName()))
                .andExpect(jsonPath("$.lastBucketCodeLoadStatus").isNotEmpty())
                .andExpect(jsonPath("$.condition").value(discount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.suspendedReasonMessage").isEmpty());
    }

    @Test
    void Create_CreateDiscountWithBucket_TestBucketLoadRetry_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.BUCKET);
        CreateDiscount discount = createSampleCreateDiscountWithBucket();

        // upload a csv
        azureStorage.uploadCsv(multipartFile.getInputStream(), discount.getLastBucketCodeLoadUid(),
                multipartFile.getSize());

        // introduce an anomaly to test retry
        dropAndRecoverBucketCodeLoadEntity();

        // call api to create a discount
        this.mockMvc.perform(post(discountPath).contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(discount))).andDo(log()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue())) // default state
                .andExpect(jsonPath("$.name").value(discount.getName()))
                .andExpect(jsonPath("$.description").value(discount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(discount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(discount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(discount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").value(discount.getStaticCode()))
                .andExpect(jsonPath("$.landingPageUrl").value(discount.getLandingPageUrl()))
                .andExpect(jsonPath("$.landingPageReferrer").value(discount.getLandingPageReferrer()))
                .andExpect(jsonPath("$.lastBucketCodeLoadUid").value(discount.getLastBucketCodeLoadUid()))
                .andExpect(jsonPath("$.lastBucketCodeLoadFileName").value(discount.getLastBucketCodeLoadFileName()))
                .andExpect(jsonPath("$.lastBucketCodeLoadStatus").isNotEmpty())
                .andExpect(jsonPath("$.condition").value(discount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.suspendedReasonMessage").isEmpty());

        // we have to wait for all retries to complete
        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 2);
    }

    protected void dropAndRecoverBucketCodeLoadEntity() {
        CompletableFuture.runAsync(() -> {
            log.info("#TESTING ANOMALY: Waiting for BucketCodeLoadEntity creation.");
            Awaitility
                    .with()
                    .pollDelay(1, TimeUnit.MILLISECONDS)
                    .and()
                    .pollInterval(1, TimeUnit.MILLISECONDS)
                    .await()
                    .atMost(1, TimeUnit.SECONDS)
                    .until(() -> bucketCodeLoadRepository.count() == 1);

            log.info("#TESTING ANOMALY: Get BucketCodeLoadEntity to recover before deleting it.");
            var bucketCodeLoad = bucketCodeLoadRepository.findAll().get(0);

            var recoverBucketCodeLoad = new BucketCodeLoadEntity();
            recoverBucketCodeLoad.setDiscountId(bucketCodeLoad.getDiscountId());
            recoverBucketCodeLoad.setNumberOfCodes(bucketCodeLoad.getNumberOfCodes());
            recoverBucketCodeLoad.setStatus(bucketCodeLoad.getStatus());
            recoverBucketCodeLoad.setUid(bucketCodeLoad.getUid());
            recoverBucketCodeLoad.setFileName(bucketCodeLoad.getFileName());

            log.info("#TESTING ANOMALY: Deleting all BucketCodeLoadEntity to introduce failure.");
            bucketCodeLoadRepository.deleteAll();

            log.info("#TESTING ANOMALY: Starting timer to do a recovery.");
            TimerTask task = new TimerTask() {
                public void run() {
                    log.info("#TESTING ANOMALY: Recovering BucketCodeLoadEntity and DiscountEntity after delete.");
                    bucketCodeLoadRepository.save(recoverBucketCodeLoad);
                    var discountEntity = discountRepository.findById(recoverBucketCodeLoad.getDiscountId()).orElseThrow();
                    discountEntity.setLastBucketCodeLoad(recoverBucketCodeLoad);
                    discountRepository.save(discountEntity);
                    log.info("#TESTING ANOMALY: Recovery finished.");
                }
            };

            Timer timer = new Timer("Recover");
            long delay = 5000L;
            timer.schedule(task, delay);
        });
    }

    @Test
    void Create_CreateDiscountWithBucket_Ko() throws Exception {
        initTest(DiscountCodeTypeEnum.BUCKET);
        CreateDiscount discount = createSampleCreateDiscountWithBucket();
        discount.setLastBucketCodeLoadUid(null);
        discount.setLastBucketCodeLoadFileName(null);
        this.mockMvc.perform(post(discountPath).contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.getJson(discount))).andDo(log()).andExpect(status().isBadRequest());
    }

    @Test
    void Create_CreateDiscountWithBucketWithNotExistingBucketLoadFile_Ko() throws Exception {
        initTest(DiscountCodeTypeEnum.BUCKET);
        CreateDiscount discount = createSampleCreateDiscountWithBucket();
        azureStorage.uploadCsv(multipartFile.getInputStream(), UUID.randomUUID().toString(), multipartFile.getSize());
        this.mockMvc.perform(post(discountPath).contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.getJson(discount))).andDo(log()).andExpect(status().isBadRequest());
    }

    @Test
    void Create_CreateDiscountWithoutStartDate_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);
        CreateDiscount discount = createSampleCreateDiscount();
        discount.setStartDate(null);
        this.mockMvc.perform(post(discountPath).contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.getJson(discount))).andDo(log()).andExpect(status().isBadRequest());
    }

    @Test
    void Create_CreateDiscountWithoutStaticCode_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);
        CreateDiscount discount = createSampleCreateDiscount();
        discount.setStaticCode(null);
        this.mockMvc.perform(post(discountPath).contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.getJson(discount))).andDo(log()).andExpect(status().isBadRequest());
    }

    @Test
    void Update_CreateAndUpdateDiscount_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discount = TestUtils.createSampleDiscountEntityWithStaticCode(agreement, "static_code");
        discount = discountService.createDiscount(agreement.getId(), discount).getDiscountEntity();

        UpdateDiscount updateDiscount = updatableDiscountFromDiscountEntity(discount);
        updateDiscount.setName("new_name");
        updateDiscount.setStaticCode("new_static_code");
        this.mockMvc.perform(put(discountPath + "/" + discount.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(updateDiscount))).andDo(log()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue())) // default state
                .andExpect(jsonPath("$.name").value(updateDiscount.getName()))
                .andExpect(jsonPath("$.description").value(updateDiscount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(updateDiscount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(updateDiscount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(updateDiscount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").value(updateDiscount.getStaticCode()))
                .andExpect(jsonPath("$.landingPageUrl").value(updateDiscount.getLandingPageUrl()))
                .andExpect(jsonPath("$.landingPageReferrer")
                        .value(updateDiscount.getLandingPageReferrer()))
                .andExpect(jsonPath("$.condition").value(updateDiscount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.suspendedReasonMessage").isEmpty());
    }

    @Test
    void Update_CreateAndUpdateDiscountWithLandingPage_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreement, "url",
                "referrer");
        discountEntity = discountService.createDiscount(agreement.getId(), discountEntity).getDiscountEntity();

        UpdateDiscount updateDiscount = updatableDiscountFromDiscountEntity(discountEntity);
        updateDiscount.setName("new_name");
        updateDiscount.setLandingPageUrl("new_url");
        updateDiscount.setLandingPageReferrer("new_referrer");
        this.mockMvc.perform(put(discountPath + "/" + discountEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(updateDiscount)))
                .andDo(log()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.id").value(discountEntity.getId()))
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue())) // default state
                .andExpect(jsonPath("$.name").value(updateDiscount.getName()))
                .andExpect(jsonPath("$.description").value(updateDiscount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(updateDiscount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(updateDiscount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(updateDiscount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").isEmpty())
                .andExpect(jsonPath("$.landingPageUrl").isNotEmpty())
                .andExpect(jsonPath("$.landingPageUrl").value(updateDiscount.getLandingPageUrl()))
                .andExpect(jsonPath("$.landingPageReferrer").isNotEmpty())
                .andExpect(jsonPath("$.landingPageReferrer")
                        .value(updateDiscount.getLandingPageReferrer()))
                .andExpect(jsonPath("$.condition").value(updateDiscount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.suspendedReasonMessage").isEmpty());
    }

    @Test
    void Update_CreateAndUpdateDiscountWithBucket_NoBucketChange_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreement);
        azureStorage.uploadCsv(multipartFile.getInputStream(), discountEntity.getLastBucketCodeLoadUid(), multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreement.getId(), discountEntity).getDiscountEntity();

        UpdateDiscount updateDiscount = updatableDiscountFromDiscountEntity(discountEntity);
        updateDiscount.setName("new_name");
        this.mockMvc.perform(put(discountPath + "/" + discountEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(updateDiscount)))
                .andDo(log()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.id").value(discountEntity.getId()))
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue())) // default state
                .andExpect(jsonPath("$.name").value(updateDiscount.getName()))
                .andExpect(jsonPath("$.description").value(updateDiscount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(updateDiscount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(updateDiscount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(updateDiscount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").isEmpty())
                .andExpect(jsonPath("$.landingPageUrl").isEmpty())
                .andExpect(jsonPath("$.landingPageReferrer").isEmpty())
                .andExpect(jsonPath("$.lastBucketCodeLoadUid").isNotEmpty())
                .andExpect(jsonPath("$.lastBucketCodeLoadUid")
                        .value(updateDiscount.getLastBucketCodeLoadUid()))
                .andExpect(jsonPath("$.condition").value(updateDiscount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.suspendedReasonMessage").isEmpty());
    }

    @Test
    void Update_CreateAndUpdateDiscountWithBucket_WithBucketChange_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreement);
        azureStorage.uploadCsv(multipartFile.getInputStream(), discountEntity.getLastBucketCodeLoadUid(), multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreement.getId(), discountEntity).getDiscountEntity();

        // load codes
        bucketLoadUtils.storeCodesBucket(discountEntity.getId());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 2);

        UpdateDiscount updateDiscount = updatableDiscountFromDiscountEntity(discountEntity);
        updateDiscount.setName("new_name");
        updateDiscount.setLastBucketCodeLoadUid(TestUtils.generateDiscountBucketCodeUid());
        updateDiscount.setLastBucketCodeLoadFileName("new-codes.csv");
        azureStorage.uploadCsv(multipartFile.getInputStream(), updateDiscount.getLastBucketCodeLoadUid(), multipartFile.getSize());

        this.mockMvc.perform(put(discountPath + "/" + discountEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(updateDiscount)))
                .andDo(log()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.id").value(discountEntity.getId()))
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue())) // default state
                .andExpect(jsonPath("$.name").value(updateDiscount.getName()))
                .andExpect(jsonPath("$.description").value(updateDiscount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(updateDiscount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(updateDiscount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(updateDiscount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").isEmpty())
                .andExpect(jsonPath("$.landingPageUrl").isEmpty())
                .andExpect(jsonPath("$.landingPageReferrer").isEmpty())
                .andExpect(jsonPath("$.lastBucketCodeLoadUid").value(updateDiscount.getLastBucketCodeLoadUid()))
                .andExpect(jsonPath("$.lastBucketCodeLoadFileName").value(updateDiscount.getLastBucketCodeLoadFileName()))
                .andExpect(jsonPath("$.condition").value(updateDiscount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.suspendedReasonMessage").isEmpty());
    }

    @Test
    void Get_GetDiscountBucketCodeLoadingProgess_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreement);
        azureStorage.uploadCsv(multipartFile.getInputStream(), discountEntity.getLastBucketCodeLoadUid(), multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreement.getId(), discountEntity).getDiscountEntity();

        // load codes
        bucketService.setRunningBucketLoad(discountEntity.getId());

        this.mockMvc.perform(get(discountPath + "/" + discountEntity.getId() + "/bucket-loading-progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loaded").value(0))
                .andExpect(jsonPath("$.percent").value(0));

        bucketService.performBucketLoad(discountEntity.getId());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> discountBucketCodeRepository.count() == 2);

        this.mockMvc.perform(get(discountPath + "/" + discountEntity.getId() + "/bucket-loading-progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loaded").value(2))
                .andExpect(jsonPath("$.percent").value(100));
    }

    @Test
    void Get_GetDiscount_Found() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        discountService.createDiscount(agreement.getId(), discountEntity);

        this.mockMvc.perform(get(discountPath).contentType(MediaType.APPLICATION_JSON)).andDo(log())
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isNotEmpty()).andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").isNotEmpty())
                .andExpect(jsonPath("$.items[0].productCategories", hasSize(2)));
    }

    @Test
    void Get_GetSuspendedDiscount_Found() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        discountEntity = discountService.createDiscount(agreement.getId(), discountEntity).getDiscountEntity();
        discountEntity.setState(DiscountStateEnum.SUSPENDED);
        discountEntity.setSuspendedReasonMessage("A reason");
        discountEntity = discountRepository.save(discountEntity);

        this.mockMvc.perform(get(discountPath).contentType(MediaType.APPLICATION_JSON)).andDo(log())
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isNotEmpty()).andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").isNotEmpty())
                .andExpect(jsonPath("$.items[0].productCategories", hasSize(2)))
                .andExpect(jsonPath("$.items[0].state").value(DiscountState.SUSPENDED.getValue()))
                .andExpect(jsonPath("$.items[0].suspendedReasonMessage")
                        .value(discountEntity.getSuspendedReasonMessage()));
    }

    @Test
    void Get_GetDiscount_NotFound() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);
        this.mockMvc.perform(get(discountPath).contentType(MediaType.APPLICATION_JSON)).andDo(log())
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray()).andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void Delete_DeleteDiscount_Ok() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        discountService.createDiscount(agreement.getId(), discountEntity);
        this.mockMvc.perform(delete(discountPath + "/" + discountEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)).andDo(log())
                .andExpect(status().isNoContent());
        List<DiscountEntity> discounts = discountService.getDiscounts(agreement.getId());
        Assertions.assertNotNull(discounts);
        Assertions.assertEquals(0, discounts.size());
    }

    @Test
    void Delete_DeleteDiscount_NotFound() throws Exception {
        initTest(DiscountCodeTypeEnum.STATIC);
        this.mockMvc.perform(delete(discountPath + "/" + 1).contentType(MediaType.APPLICATION_JSON))
                .andDo(log()).andExpect(status().isNotFound());
        List<DiscountEntity> discounts = discountService.getDiscounts(agreement.getId());
        Assertions.assertNotNull(discounts);
        Assertions.assertEquals(0, discounts.size());
    }

    private CreateDiscount createSampleCreateDiscountWithStaticCode() {
        CreateDiscount discount = createSampleCreateDiscount();
        discount.setStaticCode("create_discount_static_code");
        return discount;
    }

    private CreateDiscount createSampleCreateDiscountWithLandingPage() {
        CreateDiscount discount = createSampleCreateDiscount();
        discount.setLandingPageUrl("landingpage.com");
        discount.setLandingPageReferrer("referrer");
        return discount;
    }

    private CreateDiscount createSampleCreateDiscountWithBucket() {
        CreateDiscount discount = createSampleCreateDiscount();
        discount.setLastBucketCodeLoadUid(UUID.randomUUID().toString());
        discount.setLastBucketCodeLoadFileName("filename.csv");
        return discount;
    }

    private CreateDiscount createSampleCreateDiscount() {
        CreateDiscount createDiscount = new CreateDiscount();
        createDiscount.setName("create_discount_name");
        createDiscount.setDescription("create_discount_description");
        createDiscount.setDiscount(15);
        createDiscount.setCondition("create_discount_condition");
        createDiscount.setStartDate(LocalDate.now());
        createDiscount.setEndDate(LocalDate.now().plusMonths(6));
        createDiscount.setProductCategories(Arrays.asList(ProductCategory.TRAVELLING, ProductCategory.SPORTS));
        return createDiscount;
    }

    private UpdateDiscount updatableDiscountFromDiscountEntity(DiscountEntity discountEntity) {
        Discount discount = discountConverter.toDto(discountEntity);
        UpdateDiscount updateDiscount = new UpdateDiscount();
        updateDiscount.setName(discount.getName());
        updateDiscount.setDescription(discount.getDescription());
        updateDiscount.setCondition(discount.getCondition());
        updateDiscount.setStartDate(discount.getStartDate());
        updateDiscount.setEndDate(discount.getEndDate());
        updateDiscount.setStaticCode(discount.getStaticCode());
        updateDiscount.setLandingPageUrl(discount.getLandingPageUrl());
        updateDiscount.setLandingPageReferrer(discount.getLandingPageReferrer());
        updateDiscount.setProductCategories(discount.getProductCategories());
        updateDiscount.setLastBucketCodeLoadUid(discount.getLastBucketCodeLoadUid());
        updateDiscount.setLastBucketCodeLoadFileName(discount.getLastBucketCodeLoadFileName());
        return updateDiscount;
    }
}
