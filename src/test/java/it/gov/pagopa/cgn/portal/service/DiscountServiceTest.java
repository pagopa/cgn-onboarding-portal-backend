package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.*;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.AddressRepository;
import it.gov.pagopa.cgn.portal.support.TestReferentRepository;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("dev")
class DiscountServiceTest extends IntegrationAbstractTest {

    private static final String STATIC_CODE = "static_code";
    private static final String URL = "www.landingpage.com";
    private static final String REFERRER = "referrer";

    @Autowired
    private BackofficeAgreementService backofficeAgreementService;

    @Autowired
    private TestReferentRepository testReferentRepository;

    @Autowired
    private AddressRepository addressRepository;

    private AgreementEntity agreementEntity;

    @BeforeEach
    void init() {
        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    void setProfileDiscountType(DiscountCodeTypeEnum discountType) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId()).orElseThrow();
        profileEntity.setDiscountCodeType(discountType);
        //to avoid LazyInitializationException
        profileEntity.setReferent(testReferentRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setAddressList(addressRepository.findByProfileId(profileEntity.getId()));
        profileService.updateProfile(agreementEntity.getId(), profileEntity);
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    @Test
    void Create_CreateDiscountWithValidData_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());

    }

    @Test
    void Create_CreateDiscountWithStaticCode_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithStaticCode(agreementEntity, STATIC_CODE);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNotNull(discountEntity.getStaticCode());
        Assertions.assertEquals(discountEntity.getStaticCode(), STATIC_CODE);
        Assertions.assertNull(discountEntity.getLandingPageUrl());
        Assertions.assertNull(discountEntity.getLandingPageReferrer());
    }

    @Test
    void Create_CreateDiscountWithLandingPage_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity, URL, REFERRER);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        Assertions.assertNotNull(discountEntity.getId());
        Assertions.assertNotNull(discountEntity.getAgreement());
        Assertions.assertNotNull(discountEntity.getProducts());
        Assertions.assertFalse(discountEntity.getProducts().isEmpty());
        Assertions.assertNotNull(discountEntity.getProducts().get(0));
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getProductCategory());
        Assertions.assertNotNull(discountEntity.getProducts().get(0).getDiscount());
        Assertions.assertNull(discountEntity.getStaticCode());
        Assertions.assertNotNull(discountEntity.getLandingPageUrl());
        Assertions.assertEquals(discountEntity.getLandingPageUrl(), URL);
        Assertions.assertNotNull(discountEntity.getLandingPageReferrer());
        Assertions.assertEquals(discountEntity.getLandingPageReferrer(), REFERRER);
    }

    @Test
    void Create_CreateDiscountWithStaticCodeAndOperatorAPI_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.API);

        //discountEntity have static code, but profile is API. Static code not saved.
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
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
    }

    @Test
    void Create_CreateDiscountWithLandingPageAndOperatorAPI_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.API);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        //discountEntity have landing page, but profile is API. Static code not saved.
        discountEntity.setLandingPageUrl("xxxx");
        discountEntity.setLandingPageReferrer("xxxx");
        discountEntity.setStaticCode(null);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
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

    }

    @Test
    void Create_CreateDiscountWithoutProducts_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setProducts(null);
        Assertions.assertThrows(Exception.class, () -> discountService.createDiscount(agreementEntity.getId(), discountEntity));
    }

    @Test
    void Create_CreateDiscountWithoutDiscountValue_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDiscountValue(null);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
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
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
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
        Assertions.assertEquals(discountEntity.getProducts().size(), discountDB.getProducts().size());
        IntStream.range(0, discountEntity.getProducts().size()).forEach(idx ->
                Assertions.assertEquals(
                        discountEntity.getProducts().get(idx).getProductCategory(),
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
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
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
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        Long discountEntityId = discountEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.getDiscountById("invalid", discountEntityId));
    }

    @Test
    void Update_UpdateDiscountWithStaticCodeWithValidData_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithStaticCode(agreementEntity, STATIC_CODE);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithoutProduct(agreementEntity);

        updatedDiscount.setName("updated_name");
        updatedDiscount.setDescription("updated_description");
        updatedDiscount.setStartDate(LocalDate.now().plusDays(1));
        updatedDiscount.setEndDate(LocalDate.now().plusMonths(3));
        updatedDiscount.setDiscountValue(40);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.ENTERTAINMENT);
        productEntity.setDiscount(updatedDiscount);
        updatedDiscount.addProductList(Collections.singletonList(productEntity));
        updatedDiscount.setCondition("update_condition");
        updatedDiscount.setStaticCode("update_static_code");


        DiscountEntity dbDiscount;
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount);
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

    }

    @Test
    void Update_UpdateDiscountWithLandingPageWithValidData_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity, URL, REFERRER);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);

        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity, "updated_" + URL, "updated_" + REFERRER);
        updatedDiscount.setName("updated_name");
        updatedDiscount.setDescription("updated_description");
        updatedDiscount.setStartDate(LocalDate.now().plusDays(1));
        updatedDiscount.setEndDate(LocalDate.now().plusMonths(3));
        updatedDiscount.setDiscountValue(40);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.ENTERTAINMENT);
        productEntity.setDiscount(updatedDiscount);
        updatedDiscount.addProductList(Collections.singletonList(productEntity));
        updatedDiscount.setCondition("update_condition");

        DiscountEntity dbDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount);
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
    }

    @Test
    void Update_UpdateDiscountWithInvalidAgreementId_ThrowInvalidRequestException() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithoutProduct(agreementEntity);
        updatedDiscount.setName("updated_name");
        Long discountId = discountEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.updateDiscount("invalidAgreementId", discountId, updatedDiscount));
    }

    @Test
    void Update_UpdateDiscountWithNotUpdatedProducts_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");

        DiscountEntity dbDiscount;
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount);
        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        IntStream.range(0, updatedDiscount.getProducts().size()).forEach(index ->
                Assertions.assertEquals(updatedDiscount.getProducts().get(index), dbDiscount.getProducts().get(index)));

    }

    @Test
    void Update_UpdateDiscountWithNewProduct_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");
        DiscountProductEntity newProduct = new DiscountProductEntity();
        newProduct.setProductCategory(ProductCategoryEnum.LEARNING);
        newProduct.setDiscount(updatedDiscount);
        updatedDiscount.getProducts().add(newProduct);

        DiscountEntity dbDiscount;
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount);
        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        Assertions.assertEquals(3, updatedDiscount.getProducts().size());
        IntStream.range(0, 3).forEach(index ->
                Assertions.assertEquals(updatedDiscount.getProducts().get(index), dbDiscount.getProducts().get(index)));

    }

    @Test
    void Update_UpdateDiscountWithoutDiscountValue_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");
        updatedDiscount.setDiscountValue(null);
        DiscountProductEntity newProduct = new DiscountProductEntity();
        newProduct.setProductCategory(ProductCategoryEnum.LEARNING);
        newProduct.setDiscount(updatedDiscount);
        updatedDiscount.getProducts().add(newProduct);

        DiscountEntity dbDiscount;
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount);
        Assertions.assertEquals(updatedDiscount.getName(), dbDiscount.getName());
        Assertions.assertFalse(updatedDiscount.getProducts().isEmpty());
        Assertions.assertEquals(3, updatedDiscount.getProducts().size());
        IntStream.range(0, 3).forEach(index ->
                Assertions.assertEquals(updatedDiscount.getProducts().get(index), dbDiscount.getProducts().get(index)));
        Assertions.assertNull(dbDiscount.getDiscountValue());

    }


    @Test
    void Update_UpdateDiscountWithRequiredFieldToNull_ThrowException() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        final Long discountEntityId = discountEntity.getId();
        final String agreementId = agreementEntity.getId();
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        discountEntity.setDescription(null);
        DiscountEntity finalDiscountEntity = discountEntity;
        Assertions.assertThrows(Exception.class, () ->
                discountService.updateDiscount(agreementId, discountEntityId, finalDiscountEntity));
    }

    @Test
    void Update_UpdateDiscountNotExists_ThrowException() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDescription(null);
        Assertions.assertThrows(Exception.class,
                () -> discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), discountEntity));
    }

    @Test
    void Delete_DeleteDiscount_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
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
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_PublishSuspendedDiscount_ThrowInvalidRequestException() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        //update state to suspended
        dbDiscount.setState(DiscountStateEnum.SUSPENDED);
        dbDiscount = discountRepository.save(dbDiscount);
        Long discountId = dbDiscount.getId();
        //publish discount
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.publishDiscount(agreementId, discountId));

    }

    @Test
    void Publish_PublishDiscountWithMultiplePublishedDiscounts_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        //publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());

        DiscountEntity discountEntity2 = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount2 = discountService.createDiscount(agreementEntity.getId(), discountEntity2);
        discountService.publishDiscount(agreementEntity.getId(), dbDiscount2.getId());

        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());
    }

    @Test
    void Publish_PublishDiscountWithStartDateAfterToday_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        final String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setStartDate(LocalDate.now().plusDays(2));
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity);
        agreementEntity = agreementService.requestApproval(agreementId);
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        final Long dbDiscountId = dbDiscount.getId();
        dbDiscount = discountService.publishDiscount(agreementId, dbDiscountId);
        agreementEntity = agreementService.findById(agreementId);
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertNotNull(agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_PublishDiscountWithAgreementStartDateAfterToday_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        final String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        agreementEntity.setStartDate(LocalDate.now().plusDays(2));
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity);
        agreementEntity = agreementService.requestApproval(agreementId);
        approveAgreement();  //simulation of approved
        agreementEntity.setStartDate(LocalDate.now().plusDays(1));
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        final Long dbDiscountId = dbDiscount.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.publishDiscount(agreementId, dbDiscountId));
        agreementEntity = agreementService.findById(agreementId);
        Assertions.assertEquals(DiscountStateEnum.DRAFT, dbDiscount.getState());
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_UpdateDiscountNotRelatedToAgreement_ThrowException() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        var agreementEntity2 = agreementService.createAgreementIfNotExists("second-agreement");

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

        //publish discount
        String agreementId = agreementEntity2.getId();
        Long discountId = dbDiscount.getId();

        Assertions.assertThrows(InvalidRequestException.class,
                () -> discountService.suspendDiscount(agreementId, discountId, "whatever"));
    }


    @Test
    void Publish_FirstDiscountPublishDateNotUpdatedIfDiscountWasPublished_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getFirstDiscountPublishingDate());

        // simulating that discount was published 2 days ago
        agreementEntity.setFirstDiscountPublishingDate(LocalDate.now().minusDays(2));
        agreementEntity = agreementRepository.save(agreementEntity);

        //creating the second discount
        discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());

        //first publication date wasn't updated
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscount.getState());
        Assertions.assertEquals(LocalDate.now().minusDays(2), agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_PublishDiscountWithNotApprovedAgreement_ThrowException() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        final String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity);
        agreementEntity = agreementService.requestApproval(agreementId);

        //publish discount
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
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        String agreementId = agreementEntity.getId();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementId, discountEntity);
        agreementEntity = agreementService.requestApproval(agreementId);
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        discountService.publishDiscount(agreementId, dbDiscount.getId());
        //the first discount was already published. Starting with second
        IntStream.range(2, 7).forEach(idx -> {
            DiscountEntity discount = TestUtils.createSampleDiscountEntity(agreementEntity);
            DiscountEntity dbDiscountN = discountService.createDiscount(agreementId, discount);
            long discountId = dbDiscountN.getId();

            if (idx < 6) {
                dbDiscountN = discountService.publishDiscount(agreementId, discountId);
                Assertions.assertEquals(DiscountStateEnum.PUBLISHED, dbDiscountN.getState());
                long numPublished = discountRepository.countByAgreementIdAndState(agreementId, DiscountStateEnum.PUBLISHED);
                Assertions.assertEquals(idx, numPublished);
            } else {
                //sixth discount. Cannot publish more than 5 discount
                Assertions.assertThrows(InvalidRequestException.class,
                        () -> discountService.publishDiscount(agreementId, discountId));
            }
        });
        Assertions.assertEquals(5,
                discountRepository.countByAgreementIdAndState(agreementId, DiscountStateEnum.PUBLISHED));
    }

    @Test
    void Publish_PublishApprovedAgreement_UpdateLastModifyDate() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
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
    void Update_UpdateApprovedAgreement_UpdateLastModifyDate() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        setAdminAuth();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
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
        //update discount should be update informationLastModifyDate to today
        DiscountEntity toUpdateDiscountEntity = TestUtils.createSampleDiscountEntityWithoutProduct(agreementEntity);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setDiscount(toUpdateDiscountEntity);
        productEntity.setProductCategory(ProductCategoryEnum.LEARNING);
        toUpdateDiscountEntity.setProducts(Collections.singletonList(productEntity));
        toUpdateDiscountEntity.setDiscountValue(70);
        discountEntity = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), toUpdateDiscountEntity);

        Assertions.assertEquals(70, discountEntity.getDiscountValue());
        agreementEntity = agreementRepository.findById(agreementEntity.getId()).orElseThrow();
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getInformationLastUpdateDate());

    }

    @Test
    void Update_UpdateDiscountWithDocumentUploadedWillDeleteDocuments_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        Assertions.assertEquals(2, documentRepository.findByAgreementId(agreementEntity.getId()).size());

        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");
        discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), updatedDiscount);

        Assertions.assertEquals(0, documentRepository.findByAgreementId(agreementEntity.getId()).size());
    }

    @Test
    void Update_UpdateSuspendedDiscountUpdatedToDraft_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        dbDiscount = discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId());
        dbDiscount = discountService.suspendDiscount(agreementEntity.getId(), dbDiscount.getId(), "suspendedReason");
        discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDiscountValue(80);
        dbDiscount = discountService.updateDiscount(agreementEntity.getId(), dbDiscount.getId(), discountEntity);
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.DRAFT, dbDiscount.getState());
        Assertions.assertEquals(80, dbDiscount.getDiscountValue());

    }

    @Test
    void Update_UpdateDiscountOfRejectedAgreement_StateAgreementUpdateToDraft() {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        String agreementId = agreementEntity.getId();
        setAdminAuth();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementId, discountEntity);
        agreementEntity = agreementService.requestApproval(agreementId);
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementEntity = agreementRepository.save(agreementEntity);
        documentRepository.saveAll(saveBackofficeSampleDocuments(agreementEntity));
        agreementEntity = backofficeAgreementService.rejectAgreement(agreementId, "rejected reason message");

        DiscountEntity UpdatingDiscountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        UpdatingDiscountEntity.setDiscountValue(55);
        discountEntity = discountService.updateDiscount(agreementId, discountEntity.getId(), UpdatingDiscountEntity);

        Assertions.assertEquals(UpdatingDiscountEntity.getDiscountValue(), discountEntity.getDiscountValue());
        agreementEntity = agreementService.findById(agreementId);
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());
        Assertions.assertNull(agreementEntity.getRequestApprovalTime());
        Assertions.assertNull(agreementEntity.getBackofficeAssignee());
        List<DocumentEntity> documents = documentRepository.findByAgreementId(agreementId);
        Assertions.assertTrue(CollectionUtils.isEmpty(documents));
    }

    private void approveAgreement() {
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementEntity.setStartDate(LocalDate.now());
        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
    }
}

