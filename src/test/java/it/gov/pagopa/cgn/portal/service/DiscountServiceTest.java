package it.gov.pagopa.cgn.portal.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.*;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("dev")
class DiscountServiceTest extends IntegrationAbstractTest {

    private static final String STATIC_CODE = "static_code";
    private static final String URL = "www.landingpage.com";
    private static final String REFERRER = "referrer";
    private static String AGREEMENT_ID;

    @Autowired
    private BackofficeAgreementService backofficeAgreementService;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private AzureStorage azureStorage;

    @Autowired
    private BucketService bucketService;

    private AgreementEntity agreementEntity;

    private MockMultipartFile multipartFile;

    @BeforeEach
    void init() throws IOException {
        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
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

        AGREEMENT_ID = agreementEntity.getId();
    }

    @Test
    void Create_CreateDiscountWithValidData_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWith100PercentDiscount_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDiscountValue(100);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithStaticCode_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithStaticCode(agreementEntity,
                STATIC_CODE);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNotNull(discountEntity.getStaticCode());
        Assertions.assertEquals(STATIC_CODE, discountEntity.getStaticCode());
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertNotNull(discountEntity.getDiscountUrl());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithStaticCode_VisibleOnEyca_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithStaticCode(agreementEntity,
                STATIC_CODE);
        discountEntity.setVisibleOnEyca(true);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNotNull(discountEntity.getStaticCode());
        Assertions.assertEquals(STATIC_CODE, discountEntity.getStaticCode());
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertTrue(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithStaticCode_OfflineSalesChannel_VisibleOnEyca_Ok() {
        setProfileSalesChannel(agreementEntity, SalesChannelEnum.OFFLINE);

        // we create a STATIC_CODE discount not visible_on_eyca
        // we expect validation to fix it by setting static_code to null
        // and visible_on_eyca to true
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithStaticCode(agreementEntity,
                STATIC_CODE);
        discountEntity.setVisibleOnEyca(false);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertTrue(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscount_DoNotAllowPassedEndDate_Ko() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setEndDate(LocalDate.now().minusDays(1));

        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.createDiscount(AGREEMENT_ID, discountEntity));

    }

    @Test
    void Create_CreateDiscount_DoNotAllowEmptyEnAndDeDescriptionsIfItIsGiven_Ko() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDescriptionEn(null);
        discountEntity.setDescriptionDe(null);

        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.createDiscount(AGREEMENT_ID, discountEntity));

    }

    @Test
    void Create_CreateDiscount_DoNotAllowEmptyEnAndDeConditionsIfItIsGiven_Ko() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setConditionEn(null);
        discountEntity.setConditionDe(null);

        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.createDiscount(AGREEMENT_ID, discountEntity));

    }

    @Test
    void Create_CreateDiscountWithLandingPage_DoNotAllowNullUrl_Ko() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                URL,
                REFERRER);
        discountEntity.setLandingPageUrl(null);

        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.createDiscount(AGREEMENT_ID, discountEntity));

    }

    @Test
    void Create_CreateDiscountWithLandingPage_DoNotAllowNullReferrer_Ko() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                URL,
                REFERRER);
        discountEntity.setLandingPageReferrer(null);

        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.createDiscount(AGREEMENT_ID, discountEntity));

    }

    @Test
    void Create_CreateDiscountWithLandingPage_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                URL,
                REFERRER);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNotNull(discountEntity.getLandingPageUrl());
        Assertions.assertEquals(URL, discountEntity.getLandingPageUrl());
        Assertions.assertNotNull(discountEntity.getLandingPageReferrer());
        Assertions.assertEquals(REFERRER, discountEntity.getLandingPageReferrer());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithLandingPage_AllowNullReferrer_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                URL,
                REFERRER);
        discountEntity.setLandingPageReferrer(null);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNotNull(discountEntity.getLandingPageUrl());
        Assertions.assertEquals(URL, discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithBucketCodes_Ok() throws IOException {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertNotNull(discountEntity.getLastBucketCodeLoad().getId());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithBulkBucketCodes_Ok() throws IOException {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertNotNull(discountEntity.getLastBucketCodeLoad().getId());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithStaticCodeAndOperatorAPI_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.API);

        // discountEntity have static code, but profile is API. Static code not saved.
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithLandingPageAndOperatorAPI_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.API);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        // discountEntity have landing page, but profile is API. Static code not saved.
        discountEntity.setLandingPageUrl("xxxx");
        discountEntity.setLandingPageReferrer("xxxx");
        discountEntity.setStaticCode(null);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithBucketCodesAPI_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.API);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        // discountEntity have bucket, but profile is API. Static code not saved.
        discountEntity.setLandingPageUrl(null);
        discountEntity.setLandingPageReferrer(null);
        discountEntity.setStaticCode(null);
        discountEntity.setLastBucketCodeLoadUid(TestUtils.generateDiscountBucketCodeUid());
        discountEntity.setLastBucketCodeLoadFileName("anyname.csv");
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
        Assertions.assertNull(discountEntity.getLastBucketCodeLoad());
        Assertions.assertFalse(discountEntity.getVisibleOnEyca());
    }

    @Test
    void Create_CreateDiscountWithoutProducts_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setProducts(null);
        Assertions.assertThrows(Exception.class,
                () -> discountService.createDiscount(agreementEntity.getId(), discountEntity)
                        .getDiscountEntity());
    }

    @Test
    void Create_CreateDiscountWithoutDiscountValue_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDiscountValue(null);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getDiscountValue());
    }

    @Test
    void Get_GetDiscountList_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        List<DiscountEntity> discounts = discountService.getDiscounts(agreementEntity.getId());
        Assertions.assertNotNull(discounts);
        Assertions.assertFalse(discounts.isEmpty());
        Assertions.assertNotNull(discounts.get(0));
        DiscountEntity discountDB = discounts.get(0);
        Assertions.assertEquals(discountEntity.getAgreement().getId(), agreementEntity.getId());
        Assertions.assertEquals(discountEntity.getId(), discountDB.getId());
        Assertions.assertEquals(discountEntity.getName(), discountDB.getName());
        Assertions.assertEquals(discountEntity.getDescription(), discountDB.getDescription());
        Assertions.assertEquals(discountEntity.getCondition(), discountDB.getCondition());
        Assertions.assertEquals(discountEntity.getDiscountValue(), discountDB.getDiscountValue());
        Assertions.assertEquals(discountEntity.getState(), discountDB.getState());
        Assertions.assertEquals(discountEntity.getStartDate(), discountDB.getStartDate());
        Assertions.assertEquals(discountEntity.getEndDate(), discountDB.getEndDate());
        Assertions.assertEquals(discountEntity.getStaticCode(), discountDB.getStaticCode());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertNotNull(discountDB.getProducts());
        Assertions.assertNull(discountDB.getTestFailureReason());
        Assertions.assertEquals(discountEntity.getProducts().size(), discountDB.getProducts().size());
        IntStream.range(0, discountEntity.getProducts().size())
                .forEach(idx -> Assertions.assertEquals(discountEntity.getProducts().get(idx).getProductCategory(),
                        discountDB.getProducts().get(idx).getProductCategory()));
    }

    @Test
    void Create_CreateDiscountWithEndAfterToday_ThrowInvalidRequestException() {
        final String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setEndDate(LocalDate.now().minusDays(2));
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.createDiscount(agreementId, discountEntity));
    }

    @Test
    void Get_GetDiscountListNotFound_Ok() {
        List<DiscountEntity> discounts = discountService.getDiscounts(agreementEntity.getId());
        Assertions.assertNotNull(discounts);
        Assertions.assertTrue(discounts.isEmpty());
    }

    @Test
    void GetById_GetDiscountById_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        DiscountEntity dbDiscount = discountService.getDiscountById(agreementEntity.getId(), discountEntity.getId());
        Assertions.assertNotNull(dbDiscount);
        Assertions.assertEquals(discountEntity.getId(), dbDiscount.getId());
    }

    @Test
    void GetById_GetDiscountByIdNotFound_ThrowInvalidRequestException() {
        final String agreementId = agreementEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.getDiscountById(agreementId, 100L));
    }

    @Test
    void GetById_GetDiscountByIdWithInvalidAgreementId_ThrowInvalidRequestException() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Long discountEntityId = discountEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.getDiscountById("invalid", discountEntityId));
    }

    @Test
    void Update_UpdateDiscountWithStaticCodeWithValidData_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithStaticCode(agreementEntity,
                STATIC_CODE);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithoutProduct(agreementEntity);

        updatedDiscount.setName("updated_name");
        updatedDiscount.setDescription("updated_description");
        updatedDiscount.setStartDate(LocalDate.now().plusDays(1));
        updatedDiscount.setEndDate(LocalDate.now().plusMonths(3));
        updatedDiscount.setDiscountValue(40);
        var newDiscountUrl = "https://anotherurl.com";
        updatedDiscount.setDiscountUrl(newDiscountUrl);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.CULTURE_AND_ENTERTAINMENT);
        productEntity.setDiscount(updatedDiscount);
        updatedDiscount.addProductList(Collections.singletonList(productEntity));
        updatedDiscount.setCondition("update_condition");
        updatedDiscount.setStaticCode("update_static_code");
        updatedDiscount.setVisibleOnEyca(true);

        DiscountEntity dbDiscount = discountService.updateDiscount(agreementEntity.getId(),
                discountEntity.getId(),
                updatedDiscount).getDiscountEntity();
        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertEquals(updatedDiscount.getDescription(), dbDiscount.getDescription());
        Assertions.assertEquals(updatedDiscount.getStartDate(), dbDiscount.getStartDate());
        Assertions.assertEquals(updatedDiscount.getEndDate(), dbDiscount.getEndDate());
        Assertions.assertEquals(updatedDiscount.getDiscountValue(), dbDiscount.getDiscountValue());
        Assertions.assertNotNull(dbDiscount.getProducts());
        Assertions.assertFalse(dbDiscount.getProducts().isEmpty());
        Assertions.assertNotNull(updatedDiscount.getProducts());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        Assertions.assertEquals(updatedDiscount.getProducts().get(0), dbDiscount.getProducts().get(0));
        Assertions.assertEquals(updatedDiscount.getProducts().get(0), dbDiscount.getProducts().get(0));
        Assertions.assertEquals(updatedDiscount.getCondition(), dbDiscount.getCondition());
        Assertions.assertEquals(updatedDiscount.getStaticCode(), dbDiscount.getStaticCode());
        Assertions.assertEquals(updatedDiscount.getDiscountUrl(), dbDiscount.getDiscountUrl());
        Assertions.assertTrue(updatedDiscount.getVisibleOnEyca());
        Assertions.assertTrue(dbDiscount.getVisibleOnEyca());
    }

    @Test
    void Update_UpdateDiscountWithLandingPageWithValidData_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                URL,
                REFERRER);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                "updated_" + URL,
                "updated_" + REFERRER);
        updatedDiscount.setName("updated_name");
        updatedDiscount.setDescription("updated_description");
        updatedDiscount.setStartDate(LocalDate.now().plusDays(1));
        updatedDiscount.setEndDate(LocalDate.now().plusMonths(3));
        updatedDiscount.setDiscountValue(40);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.CULTURE_AND_ENTERTAINMENT);
        productEntity.setDiscount(updatedDiscount);
        updatedDiscount.addProductList(Collections.singletonList(productEntity));
        updatedDiscount.setCondition("update_condition");
        updatedDiscount.setVisibleOnEyca(true);

        DiscountEntity dbDiscount = discountService.updateDiscount(agreementEntity.getId(),
                discountEntity.getId(),
                updatedDiscount).getDiscountEntity();
        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertEquals(updatedDiscount.getDescription(), dbDiscount.getDescription());
        Assertions.assertEquals(updatedDiscount.getStartDate(), dbDiscount.getStartDate());
        Assertions.assertEquals(updatedDiscount.getEndDate(), dbDiscount.getEndDate());
        Assertions.assertEquals(updatedDiscount.getDiscountValue(), dbDiscount.getDiscountValue());
        Assertions.assertNotNull(dbDiscount.getProducts());
        Assertions.assertFalse(dbDiscount.getProducts().isEmpty());
        Assertions.assertNotNull(updatedDiscount.getProducts());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        Assertions.assertEquals(updatedDiscount.getProducts().get(0), dbDiscount.getProducts().get(0));
        Assertions.assertEquals(updatedDiscount.getProducts().get(0), dbDiscount.getProducts().get(0));
        Assertions.assertEquals(updatedDiscount.getCondition(), dbDiscount.getCondition());
        Assertions.assertEquals(updatedDiscount.getStaticCode(), dbDiscount.getStaticCode());
        Assertions.assertNull(updatedDiscount.getStaticCode());
        Assertions.assertEquals(updatedDiscount.getLandingPageUrl(), dbDiscount.getLandingPageUrl());
        Assertions.assertEquals(updatedDiscount.getLandingPageReferrer(), dbDiscount.getLandingPageReferrer());
        Assertions.assertTrue(updatedDiscount.getVisibleOnEyca());
        Assertions.assertTrue(dbDiscount.getVisibleOnEyca());
    }

    @Test
    void Update_UpdateDiscountToDrafWhenLandingPageUrlChanges_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                URL,
                REFERRER);
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);

        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementRepository.save(agreementEntity);

        discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());

        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                "updated_" + URL,
                REFERRER);

        DiscountEntity dbDiscount = discountService.updateDiscount(agreementEntity.getId(),
                discountEntity.getId(),
                updatedDiscount).getDiscountEntity();

        Assertions.assertTrue(DiscountStateEnum.DRAFT.equals(dbDiscount.getState()), "check landingpage failed");
    }

    @Test
    void Update_UpdateDiscountToDrafWhenReferrerChanges_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                URL,
                REFERRER);
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);

        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementRepository.save(agreementEntity);

        discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());

        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                URL,
                "updated_" + REFERRER);

        DiscountEntity dbDiscount = discountService.updateDiscount(agreementEntity.getId(),
                discountEntity.getId(),
                updatedDiscount).getDiscountEntity();

        Assertions.assertTrue(DiscountStateEnum.DRAFT.equals(dbDiscount.getState()), "check referrer failed");
    }

    @Test
    void Update_UpdateDiscountWithBucketCodesWithValidDataWithoutNewBucketLoad_Ok() throws IOException {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");
        updatedDiscount.setDescription("updated_description");
        updatedDiscount.setStartDate(LocalDate.now().plusDays(1));
        updatedDiscount.setEndDate(LocalDate.now().plusMonths(3));
        updatedDiscount.setDiscountValue(40);
        updatedDiscount.setStaticCode(null);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.CULTURE_AND_ENTERTAINMENT);
        productEntity.setDiscount(updatedDiscount);
        updatedDiscount.addProductList(Collections.singletonList(productEntity));
        updatedDiscount.setCondition("update_condition");
        updatedDiscount.setLastBucketCodeLoadUid(discountEntity.getLastBucketCodeLoadUid());
        updatedDiscount.setLastBucketCodeLoadFileName(discountEntity.getLastBucketCodeLoadFileName());

        DiscountEntity dbDiscount = discountService.updateDiscount(agreementEntity.getId(),
                discountEntity.getId(),
                updatedDiscount).getDiscountEntity();

        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertEquals(updatedDiscount.getDescription(), dbDiscount.getDescription());
        Assertions.assertEquals(updatedDiscount.getStartDate(), dbDiscount.getStartDate());
        Assertions.assertEquals(updatedDiscount.getEndDate(), dbDiscount.getEndDate());
        Assertions.assertEquals(updatedDiscount.getDiscountValue(), dbDiscount.getDiscountValue());
        Assertions.assertNotNull(dbDiscount.getProducts());
        Assertions.assertFalse(dbDiscount.getProducts().isEmpty());
        Assertions.assertNotNull(updatedDiscount.getProducts());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        Assertions.assertEquals(updatedDiscount.getProducts().get(0), dbDiscount.getProducts().get(0));
        Assertions.assertEquals(updatedDiscount.getProducts().get(0), dbDiscount.getProducts().get(0));
        Assertions.assertEquals(updatedDiscount.getCondition(), dbDiscount.getCondition());
        Assertions.assertEquals(updatedDiscount.getStaticCode(), dbDiscount.getStaticCode());
        Assertions.assertNull(updatedDiscount.getStaticCode());
        Assertions.assertNull(updatedDiscount.getLandingPageUrl(), dbDiscount.getLandingPageUrl());
        Assertions.assertNull(updatedDiscount.getLandingPageReferrer(), dbDiscount.getLandingPageReferrer());
        Assertions.assertEquals(updatedDiscount.getLastBucketCodeLoadUid(),
                dbDiscount.getLastBucketCodeLoad().getUid());
        Assertions.assertEquals(updatedDiscount.getLastBucketCodeLoadFileName(),
                dbDiscount.getLastBucketCodeLoad().getFileName());
    }

    @Test
    void Update_UpdateDiscountWithBucketCodesWithNewBucketLoadInProcessing_Ko() throws IOException {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        updatedDiscount.setName("updated_name");
        updatedDiscount.setDescription("updated_description");
        updatedDiscount.setStartDate(LocalDate.now().plusDays(1));
        updatedDiscount.setEndDate(LocalDate.now().plusMonths(3));
        updatedDiscount.setDiscountValue(40);
        updatedDiscount.setStaticCode(null);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.CULTURE_AND_ENTERTAINMENT);
        productEntity.setDiscount(updatedDiscount);
        updatedDiscount.addProductList(Collections.singletonList(productEntity));
        updatedDiscount.setCondition("update_condition");
        String agreementId = agreementEntity.getId();
        Long discountId = discountEntity.getId();
        BucketCodeLoadEntity bucketCodeLoad = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                .getId()).orElseThrow();
        bucketCodeLoad.setStatus(BucketCodeLoadStatusEnum.PENDING);
        bucketCodeLoadRepository.saveAndFlush(bucketCodeLoad);
        Exception exception = Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.updateDiscount(agreementId, discountId, updatedDiscount));

        Assert.assertTrue(exception.getMessage(),exception.getMessage().equals(ErrorCodeEnum.CANNOT_UPDATE_DISCOUNT_BUCKET_WHILE_PROCESSING_IS_RUNNING.getValue()));
    }

    @Test
    void Update_UpdateDiscountWithInvalidAgreementId_ThrowInvalidRequestException() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithoutProduct(agreementEntity);
        updatedDiscount.setName("updated_name");
        Long discountId = discountEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.updateDiscount("invalidAgreementId",
                        discountId,
                        updatedDiscount));
    }

    @Test
    void Update_UpdateDiscountWithNotUpdatedProducts_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");

        DiscountEntity dbDiscount;
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount)
                .getDiscountEntity();
        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        IntStream.range(0, updatedDiscount.getProducts().size())
                .forEach(index -> Assertions.assertEquals(updatedDiscount.getProducts().get(index),
                        dbDiscount.getProducts().get(index)));

    }

    @Test
    void Update_UpdateDiscountWithNewProduct_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");
        DiscountProductEntity newProduct = new DiscountProductEntity();
        newProduct.setProductCategory(ProductCategoryEnum.LEARNING);
        newProduct.setDiscount(updatedDiscount);
        updatedDiscount.getProducts().add(newProduct);

        DiscountEntity dbDiscount;
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount)
                .getDiscountEntity();
        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        Assertions.assertEquals(2, updatedDiscount.getProducts().size());
        IntStream.range(0, 2)
                .forEach(index -> Assertions.assertEquals(updatedDiscount.getProducts().get(index),
                        dbDiscount.getProducts().get(index)));

    }

    @Test
    void Update_UpdateDiscountWithoutDiscountValue_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");
        updatedDiscount.setDiscountValue(null);
        DiscountProductEntity newProduct = new DiscountProductEntity();
        newProduct.setProductCategory(ProductCategoryEnum.LEARNING);
        newProduct.setDiscount(updatedDiscount);
        updatedDiscount.getProducts().add(newProduct);

        DiscountEntity dbDiscount;
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount)
                .getDiscountEntity();
        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        Assertions.assertEquals(2, updatedDiscount.getProducts().size());
        IntStream.range(0, 2)
                .forEach(index -> Assertions.assertEquals(updatedDiscount.getProducts().get(index),
                        dbDiscount.getProducts().get(index)));
        Assertions.assertNull(dbDiscount.getDiscountValue());

    }

    @Test
    void Update_UpdateDiscountWithRequiredFieldToNull_ThrowException() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        final Long discountEntityId = discountEntity.getId();
        final String agreementId = agreementEntity.getId();
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        discountEntity.setDescription(null);
        DiscountEntity finalDiscountEntity = discountEntity;
        Assertions.assertThrows(Exception.class,
                () -> discountService.updateDiscount(agreementId,
                        discountEntityId,
                        finalDiscountEntity));
    }

    @Test
    void Update_UpdateDiscountNotExists_ThrowException() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDescription(null);
        Assertions.assertThrows(Exception.class,
                () -> discountService.updateDiscount(agreementEntity.getId(),
                        discountEntity.getId(),
                        discountEntity));
    }

    @Test
    void Delete_DeleteDiscount_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        Long discountId = discountEntity.getId();

        Assertions.assertDoesNotThrow(() -> discountService.deleteDiscount(agreementEntity.getId(), discountId));
    }

    @Test
    void Delete_DeleteDiscountNotExists_Ok() {
        Assertions.assertThrows(Exception.class,
                () -> discountService.deleteDiscount(agreementEntity.getId(), Long.MAX_VALUE));
    }

    @Test
    void Publish_PublishDiscountWithApprovedAgreement_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity); // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // await for view to be refreshed
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> publishedProductCategoryRepository.findAll().size() >= 1);

        // check that materialized view should contain this discount categories
        var discountProductCategories = dbDiscount.getProducts()
                .stream()
                .map(DiscountProductEntity::getProductCategory)
                .collect(Collectors.toList());
        var publishedProductCategories = publishedProductCategoryRepository.findAll();
        Assertions.assertEquals(discountProductCategories.size(), publishedProductCategories.size());
        publishedProductCategories.forEach(c -> {
            Assertions.assertTrue(discountProductCategories.contains(c.getProductCategory()));
        });
    }

    @Test
    void Publish_PublishDiscountWithPastStartDate_ShouldUpdateStartDate_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setStartDate(LocalDate.now().minusDays(30));
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());
        Assertions.assertEquals(LocalDate.now(), dbDiscount.getStartDate());

        // await for view to be refreshed
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> publishedProductCategoryRepository.findAll().size() >= 1);

        // check that materialized view should contain this discount categories
        var discountProductCategories = dbDiscount.getProducts()
                .stream()
                .map(DiscountProductEntity::getProductCategory)
                .collect(Collectors.toList());
        var publishedProductCategories = publishedProductCategoryRepository.findAll();
        Assertions.assertEquals(discountProductCategories.size(), publishedProductCategories.size());
        publishedProductCategories.forEach(c -> {
            Assertions.assertTrue(discountProductCategories.contains(c.getProductCategory()));
            Assertions.assertEquals(1L, (long) c.getNewDiscounts());
        });
    }

    @Test
    void Publish_PublishSuspendedDiscount_ThrowInvalidRequestException() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        // update state to suspended
        dbDiscount.setState(DiscountStateEnum.SUSPENDED);
        dbDiscount = discountRepository.save(dbDiscount);
        Long discountId = dbDiscount.getId();
        // publish discount
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.publishDiscount(agreementId, discountId));

    }

    @Test
    void Publish_PublishDraftOfflineDiscount_Ok() {
        setProfileSalesChannel(agreementEntity, SalesChannelEnum.OFFLINE);
        setProfileDiscountType(agreementEntity, null); // offline merchant don't have discount type

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // no test done because it's a physical store discount

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount should work directly from draft
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());
    }

    @Test
    void Publish_PublishDraftOnlineDiscount_ThrowInvalidRequestException() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // no status update

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);

        Long discountId = dbDiscount.getId();
        // publish discount
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.publishDiscount(agreementId, discountId));
    }

    @Test
    void Publish_PublishTestFailedOnlineDiscount_ThrowInvalidRequestException() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test failed
        dbDiscount.setState(DiscountStateEnum.TEST_FAILED);
        dbDiscount.setTestFailureReason("any reason");
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);

        Long discountId = dbDiscount.getId();
        // publish discount
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.publishDiscount(agreementId, discountId));
    }

    @Test
    void Publish_PublishDiscountWithMultiplePublishedDiscounts_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount2 = discountService.createDiscount(agreementEntity.getId(), discountEntity2)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount2.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount2 = discountRepository.save(dbDiscount2);

        discountService.publishDiscount(agreementEntity.getId(), dbDiscount2.getId());

        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());
    }

    @Test
    void Publish_PublishDiscountWithStartDateAfterToday_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        final String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setStartDate(LocalDate.now().plusDays(2));
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity).getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementId);
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        // publish discount
        final Long dbDiscountId = dbDiscount.getId();
        dbDiscount = discountService.publishDiscount(agreementId, dbDiscountId);
        agreementEntity = agreementService.findAgreementById(agreementId);
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertNotNull(agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_PublishDiscountWithBucketInProcessing_Ko() throws IOException {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.BUCKET);

        final String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        discountEntity.setStartDate(LocalDate.now().plusDays(2));
        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity).getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementId);
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        // publish discount
        final Long dbDiscountId = dbDiscount.getId();
        BucketCodeLoadEntity bucketCodeLoad = bucketCodeLoadRepository.findById(discountEntity.getLastBucketCodeLoad()
                .getId()).get();
        bucketCodeLoad.setStatus(BucketCodeLoadStatusEnum.PENDING);
        bucketCodeLoadRepository.saveAndFlush(bucketCodeLoad);
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.publishDiscount(agreementId, dbDiscountId));

    }

    @Test
    void Publish_PublishDiscountWithAgreementStartDateAfterToday_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        final String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        agreementEntity.setStartDate(LocalDate.now().plusDays(2));
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity).getDiscountEntity();
        agreementEntity = agreementService.requestApproval(agreementId);
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity.setStartDate(LocalDate.now().plusDays(1));
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        // publish discount
        final Long dbDiscountId = dbDiscount.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.publishDiscount(agreementId, dbDiscountId));
        agreementEntity = agreementService.findAgreementById(agreementId);
        Assertions.assertEquals(DiscountStateEnum.DRAFT, dbDiscount.getState());
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_UpdateDiscountNotRelatedToAgreement_ThrowException() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        var agreementEntity2 = agreementService.createAgreementIfNotExists("second-agreement", EntityType.PRIVATE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        String agreementId = agreementEntity2.getId();
        Long discountId = dbDiscount.getId();

        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.suspendDiscount(agreementId, discountId, "whatever"));
    }

    @Test
    void Publish_FirstDiscountPublishDateNotUpdatedIfDiscountWasPublished_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // simulating that discount was published 2 days ago
        agreementEntity.setFirstDiscountPublishingDate(LocalDate.now().minusDays(2));
        agreementEntity = agreementRepository.save(agreementEntity);

        // creating the second discount
        discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());

        // first publication date wasn't updated
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now().minusDays(2), agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_PublishDiscountWithNotApprovedAgreement_ThrowException() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        final String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity).getDiscountEntity();
        agreementEntity = agreementService.requestApproval(agreementId);

        // publish discount
        final Long dbDiscountId = dbDiscount.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.publishDiscount(agreementId, dbDiscountId));

        List<DiscountEntity> discounts = discountService.getDiscounts(agreementId);
        Assertions.assertNotNull(discounts);
        Assertions.assertFalse(discounts.isEmpty());
        discountEntity = discounts.stream().filter(d -> d.getId().equals(dbDiscountId)).findFirst().orElseThrow();
        Assertions.assertEquals(DiscountStateEnum.DRAFT, discountEntity.getState());

    }

    @Test
    void Publish_PublishMoreThanFiveDiscounts_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity).getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementId);
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        // publish discount
        discountService.publishDiscount(agreementId, dbDiscount.getId());
        // the first discount was already published. Starting with second
        IntStream.range(2, 7).forEach(idx -> {
            DiscountEntity discount = TestUtils.createSampleDiscountEntity(agreementEntity);
            DiscountEntity dbDiscountN = discountService.createDiscount(agreementId, discount).getDiscountEntity();

            // simulate test passed
            dbDiscountN.setState(DiscountStateEnum.TEST_PASSED);
            dbDiscountN = discountRepository.save(dbDiscountN);

            long discountId = dbDiscountN.getId();

            if (idx < 6) {
                dbDiscountN = discountService.publishDiscount(agreementId, discountId);
                Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscountN.getState());
                long numPublished = discountRepository.countByAgreementIdAndStateAndEndDateGreaterThan(agreementId,
                        DiscountStateEnum.PUBLISHED,
                        LocalDate.now());
                Assertions.assertEquals(idx, numPublished);
            } else {
                // sixth discount. Cannot publish more than 5 discount
                Assertions.assertThrows(InvalidRequestException.class,
                        () -> discountService.publishDiscount(agreementId, discountId));
            }
        });
        Assertions.assertEquals(5,
                discountRepository.countByAgreementIdAndState(agreementId,
                        DiscountStateEnum.PUBLISHED));
    }

    @Test
    void Publish_PublishApprovedAgreement_UpdateLastModifyDate() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        setAdminAuth();
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementEntity = agreementRepository.save(agreementEntity);
        documentRepository.saveAll(saveBackofficeSampleDocuments(agreementEntity));
        agreementEntity = backofficeAgreementService.approveAgreement(agreementEntity.getId());

        discountService.publishDiscount(this.agreementEntity.getId(), discountEntity.getId());
        this.agreementEntity = agreementRepository.findById(this.agreementEntity.getId()).orElseThrow();
        Assertions.assertEquals(LocalDate.now(), this.agreementEntity.getInformationLastUpdateDate());

    }

    @Test
    void SuspendingLastRemainingOnlineDiscount_ShouldRefreshOnlineMerchantView() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved

        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> onlineMerchantRepository.findAll().size() >= 1);

        // assert the merchant is in the view
        var onlineMerchantEntities = onlineMerchantRepository.findAll();
        Assertions.assertEquals(1, onlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), onlineMerchantEntities.get(0).getId());

        // unpublish discount
        discountService.suspendDiscount(agreementEntity.getId(), dbDiscount.getId(), "This a test");

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> onlineMerchantRepository.findAll().size() <= 0);

        // assert the merchant is not in the view anymore
        onlineMerchantEntities = onlineMerchantRepository.findAll();
        Assertions.assertEquals(0, onlineMerchantEntities.size());
    }

    @Test
    void SuspendingLastRemainingOfflineDiscount_ShouldRefreshOfflineMerchantView() {
        setProfileSalesChannel(agreementEntity, SalesChannelEnum.OFFLINE);
        setProfileDiscountType(agreementEntity, null); // offline merchant don't have discount type

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved

        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> offlineMerchantRepository.findAll().size() >= 1);

        // assert the merchant is in the view
        var offlineMerchantEntities = offlineMerchantRepository.findAll();
        Assertions.assertEquals(1, offlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), offlineMerchantEntities.get(0).getId());

        // unpublish discount
        discountService.suspendDiscount(agreementEntity.getId(), dbDiscount.getId(), "This a test");

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> offlineMerchantRepository.findAll().size() <= 0);

        // assert the merchant is not in the view anymore
        offlineMerchantEntities = offlineMerchantRepository.findAll();
        Assertions.assertEquals(0, offlineMerchantEntities.size());
    }

    @Test
    void SuspendingLastRemainingBothDiscount_ShouldRefreshBothOnlineAndOfflineMerchantView() {
        setProfileSalesChannel(agreementEntity, SalesChannelEnum.BOTH);
        setProfileDiscountType(agreementEntity,
                DiscountCodeTypeEnum.STATIC); // offline merchant don't have discount type

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved

        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> onlineMerchantRepository.findAll().size() >= 1);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> offlineMerchantRepository.findAll().size() >= 1);

        // assert the merchant is in the view
        var onlineMerchantEntities = onlineMerchantRepository.findAll();
        Assertions.assertEquals(1, onlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), onlineMerchantEntities.get(0).getId());

        var offlineMerchantEntities = offlineMerchantRepository.findAll();
        Assertions.assertEquals(1, offlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), offlineMerchantEntities.get(0).getId());

        // unpublish discount
        discountService.suspendDiscount(agreementEntity.getId(), dbDiscount.getId(), "This a test");

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> onlineMerchantRepository.findAll().size() <= 0);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> offlineMerchantRepository.findAll().size() <= 0);

        // assert the merchant is not in the view anymore
        onlineMerchantEntities = onlineMerchantRepository.findAll();
        Assertions.assertEquals(0, onlineMerchantEntities.size());

        offlineMerchantEntities = offlineMerchantRepository.findAll();
        Assertions.assertEquals(0, offlineMerchantEntities.size());
    }

    @Test
    void SuspendingAOneOfManyBothDiscount_ShouldNotRefreshBothOnlineAndOfflineMerchantView() {
        setProfileSalesChannel(agreementEntity, SalesChannelEnum.BOTH);
        setProfileDiscountType(agreementEntity,
                DiscountCodeTypeEnum.STATIC); // offline merchant don't have discount type

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved

        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish first discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // create and publish the second discount
        discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());

        // assert that published discounts are 2
        Assertions.assertEquals(2,
                discountRepository.countByAgreementIdAndState(agreementEntity.getId(),
                        DiscountStateEnum.PUBLISHED));

        // await for view to be refreshed
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> onlineMerchantRepository.findAll().size() >= 1);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> offlineMerchantRepository.findAll().size() >= 1);

        // assert the merchant is in the view
        var onlineMerchantEntities = onlineMerchantRepository.findAll();
        Assertions.assertEquals(1, onlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), onlineMerchantEntities.get(0).getId());

        var offlineMerchantEntities = offlineMerchantRepository.findAll();
        Assertions.assertEquals(1, offlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), offlineMerchantEntities.get(0).getId());

        // unpublish discount
        discountService.suspendDiscount(agreementEntity.getId(), dbDiscount.getId(), "This a test");

        // await until there is only one published discount
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> discountRepository.countByAgreementIdAndState(agreementEntity.getId(),
                        DiscountStateEnum.PUBLISHED) <= 1);

        // assert the merchant is still in the view because there is another published discount
        onlineMerchantEntities = onlineMerchantRepository.findAll();
        Assertions.assertEquals(1, onlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), onlineMerchantEntities.get(0).getId());

        offlineMerchantEntities = offlineMerchantRepository.findAll();
        Assertions.assertEquals(1, offlineMerchantEntities.size());
        Assertions.assertEquals(agreementEntity.getId(), offlineMerchantEntities.get(0).getId());
    }

    @Test
    void SuspendingOneDiscount_ShouldRefreshPublishedProductCategoryView() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved

        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // await for view to be refreshed
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> publishedProductCategoryRepository.findAll().size() >= 1);

        // suspend discount
        dbDiscount = discountService.suspendDiscount(agreementEntity.getId(), dbDiscount.getId(), "This a test");
        Assertions.assertEquals(DiscountStateEnum.SUSPENDED, dbDiscount.getState());

        // await for view to be refreshed
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> publishedProductCategoryRepository.findAll().size() <= 0);

        // check that materialized view should NOT contain this discount categories
        // specifically just one unpublished discount should leave an empty view
        var publishedProductCategories = publishedProductCategoryRepository.findAll();
        Assertions.assertEquals(0, publishedProductCategories.size());
    }

    @Test
    void UnpublishingOneDiscount_ShouldRefreshPublishedProductCategoryView() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved

        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // await for view to be refreshed
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> publishedProductCategoryRepository.findAll().size() >= 1);

        // unpublish discount
        dbDiscount = discountService.unpublishDiscount(agreementEntity.getId(), dbDiscount.getId());
        Assertions.assertEquals(DiscountStateEnum.DRAFT, dbDiscount.getState());

        // await for view to be refreshed
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> publishedProductCategoryRepository.findAll().size() <= 0);

        // check that materialized view should NOT contain this discount categories
        // specifically just one unpublished discount should leave an empty view
        var publishedProductCategories = publishedProductCategoryRepository.findAll();
        Assertions.assertEquals(0, publishedProductCategories.size());
    }

    @Test
    void TestDiscount() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved

        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        // test discount
        dbDiscount = discountService.testDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.TEST_PENDING, dbDiscount.getState());
    }

    @Test
    void Update_UpdateApprovedAgreement_UpdateLastModifyDate() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        setAdminAuth();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        // simulate test passed
        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementEntity = agreementRepository.save(agreementEntity);
        documentRepository.saveAll(saveBackofficeSampleDocuments(agreementEntity));
        agreementEntity = backofficeAgreementService.approveAgreement(agreementEntity.getId());

        discountEntity = discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        // simulate publishing was made 3 days ago
        agreementEntity = agreementRepository.findById(agreementEntity.getId()).orElseThrow();
        agreementEntity.setInformationLastUpdateDate(LocalDate.now().minusDays(3));
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertEquals(LocalDate.now().minusDays(3), agreementEntity.getInformationLastUpdateDate());
        // update discount should be update informationLastModifyDate to today
        DiscountEntity toUpdateDiscountEntity = TestUtils.createSampleDiscountEntityWithoutProduct(agreementEntity);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setDiscount(toUpdateDiscountEntity);
        productEntity.setProductCategory(ProductCategoryEnum.LEARNING);
        toUpdateDiscountEntity.setProducts(Collections.singletonList(productEntity));
        toUpdateDiscountEntity.setDiscountValue(70);
        discountEntity = discountService.updateDiscount(agreementEntity.getId(),
                discountEntity.getId(),
                toUpdateDiscountEntity).getDiscountEntity();

        Assertions.assertEquals(70, discountEntity.getDiscountValue());
        agreementEntity = agreementRepository.findById(agreementEntity.getId()).orElseThrow();
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getInformationLastUpdateDate());

    }

    @Test
    void Update_UpdateDiscountWithDocumentUploadedWillDeleteDocuments_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();
        saveSampleDocuments(agreementEntity);
        Assertions.assertEquals(2, documentRepository.findByAgreementId(agreementEntity.getId()).size());

        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");
        discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount)
                .getDiscountEntity();

        Assertions.assertEquals(0, documentRepository.findByAgreementId(agreementEntity.getId()).size());
    }

    @Test
    void Update_UpdateSuspendedDiscountUpdatedToDraft_Ok() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity)
                .getDiscountEntity();

        // simulate test passed
        dbDiscount.setState(DiscountStateEnum.TEST_PASSED);
        dbDiscount = discountRepository.save(dbDiscount);

        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity = approveAgreement(agreementEntity);  // simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        // publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        dbDiscount = discountService.suspendDiscount(agreementEntity.getId(), dbDiscount.getId(), "suspendedReason");
        discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDiscountValue(80);
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), dbDiscount.getId(), discountEntity)
                .getDiscountEntity();
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.DRAFT, dbDiscount.getState());
        Assertions.assertEquals(80, dbDiscount.getDiscountValue());

    }

    @Test
    void Update_UpdateDiscountOfRejectedAgreement_StateAgreementUpdateToDraft() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        String agreementId = agreementEntity.getId();
        setAdminAuth();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementId, discountEntity).getDiscountEntity();
        agreementEntity = agreementService.requestApproval(agreementId);
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementEntity = agreementRepository.save(agreementEntity);
        documentRepository.saveAll(saveBackofficeSampleDocuments(agreementEntity));
        agreementEntity = backofficeAgreementService.rejectAgreement(agreementId, "rejected reason message");

        DiscountEntity UpdatingDiscountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        UpdatingDiscountEntity.setDiscountValue(55);
        discountEntity = discountService.updateDiscount(agreementId, discountEntity.getId(), UpdatingDiscountEntity)
                .getDiscountEntity();

        Assertions.assertEquals(UpdatingDiscountEntity.getDiscountValue(), discountEntity.getDiscountValue());
        agreementEntity = agreementService.findAgreementById(agreementId);
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());
        Assertions.assertNull(agreementEntity.getRequestApprovalTime());
        Assertions.assertNull(agreementEntity.getBackofficeAssignee());
        List<DocumentEntity> documents = documentRepository.findByAgreementId(agreementId);
        Assertions.assertTrue(CollectionUtils.isEmpty(documents));
    }

    @Test
    void GetDiscountBucketCodeLoadingProgess() throws IOException {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        azureStorage.uploadCsv(multipartFile.getBytes(),
                discountEntity.getLastBucketCodeLoadUid(),
                multipartFile.getSize());
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        bucketService.setRunningBucketLoad(discountEntity.getId());

        var progress = discountService.getDiscountBucketCodeLoadingProgess(discountEntity.getAgreement().getId(),
                discountEntity.getId());

        Assertions.assertEquals(0, progress.getLoaded());
        Assertions.assertEquals(0, progress.getPercent());

        bucketService.performBucketLoad(discountEntity.getId());

        progress = discountService.getDiscountBucketCodeLoadingProgess(discountEntity.getAgreement().getId(),
                discountEntity.getId());

        Assertions.assertEquals(2, progress.getLoaded());
        Assertions.assertEquals(100, progress.getPercent());
    }

    @Test
    void getDiscountById_DiscountNotFoundOrInvalidAgreement_InvalidRequestException() throws IOException {
        Exception exception = Assertions.assertThrows(InvalidRequestException.class, () -> {
            discountService.getDiscountById("fake id",
                    0L);
        });

        Assertions.assertEquals(ErrorCodeEnum.DISCOUNT_NOT_FOUND.getValue(),exception.getMessage());
    }

    @Test
    void findAgreementById_AgreementNotFound_InvalidRequestException() throws IOException {
        Exception exception = Assertions.assertThrows(InvalidRequestException.class, () -> {
            agreementServiceLight.findAgreementById("fake id");
        });

        Assertions.assertEquals(ErrorCodeEnum.AGREEMENT_NOT_FOUND.getValue(),exception.getMessage());
    }

    @Test
    void findDiscountById_AgreementNotFound_InvalidRequestException() throws IOException {
        Exception exception = Assertions.assertThrows(InvalidRequestException.class, () -> {
            discountService.findDiscountById(0L);
        });

        Assertions.assertEquals(ErrorCodeEnum.DISCOUNT_NOT_FOUND.getValue(),exception.getMessage());
    }

    @Test
    void checkDiscountRelatedSameAgreement_DiscountNotRelatedToSameAgreement_InvalidRequestException() throws IOException {
        AgreementEntity agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);

        final DiscountEntity discountEntity = discountService.createDiscount(agreementEntity.getId(),
                TestUtils.createSampleDiscountEntity(agreementEntity)).getDiscountEntity();

        Exception exception = Assertions.assertThrows(InvalidRequestException.class, () -> {
            discountService.checkDiscountRelatedSameAgreement(discountEntity,"fake agreement id");
        });

        Assertions.assertEquals(ErrorCodeEnum.DISCOUNT_NOT_RELATED_TO_AGREEMENT_PROVIDED.getValue(),exception.getMessage());
    }

    @Test
    void Create_CreateDiscount_WithMoreThanTwoCategories_Ko() {
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity).getDiscountEntity();

        DiscountProductEntity productEntity0 = new DiscountProductEntity();
        productEntity0.setProductCategory(ProductCategoryEnum.CULTURE_AND_ENTERTAINMENT);
        productEntity0.setDiscount(discountEntity);

        DiscountProductEntity productEntity1 = new DiscountProductEntity();
        productEntity1.setProductCategory(ProductCategoryEnum.HEALTH);
        productEntity1.setDiscount(discountEntity);

        DiscountProductEntity productEntity2 = new DiscountProductEntity();
        productEntity2.setProductCategory(ProductCategoryEnum.HOME);
        productEntity2.setDiscount(discountEntity);

        discountEntity.addProductList(Arrays.asList(productEntity0, productEntity1, productEntity2));

        DiscountEntity finalDiscountEntity = discountEntity;
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.createDiscount(AGREEMENT_ID, finalDiscountEntity));
    }

}
