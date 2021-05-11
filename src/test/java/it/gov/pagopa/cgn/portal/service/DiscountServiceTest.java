package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.AddressRepository;
import it.gov.pagopa.cgn.portal.support.TestReferentRepository;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("dev")
class DiscountServiceTest extends IntegrationAbstractTest {

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

    @Test
    void Create_CreateDiscountWithValidData_Ok() {
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
    void Create_CreateDiscountWithStaticCodeAndOperatorAPI_Ok() {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId()).orElseThrow();
        profileEntity.setDiscountCodeType(DiscountCodeTypeEnum.API);
        //to avoid LazyInitializationException
        profileEntity.setReferent(testReferentRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setAddressList(addressRepository.findByProfileId(profileEntity.getId()));

        profileService.updateProfile(agreementEntity.getId(), profileEntity);

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

    }

    @Test
    void Create_CreateDiscountWithoutProducts_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setProducts(null);
        Assertions.assertThrows(Exception.class, ()-> discountService.createDiscount(agreementEntity.getId(), discountEntity));
    }

    @Test
    void Get_GetDiscountList_Ok() {
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
        IntStream.range(0, discountEntity.getProducts().size()).forEach(idx -> {
            Assertions.assertEquals(
                    discountEntity.getProducts().get(idx).getProductCategory(),
                    discountDB.getProducts().get(idx).getProductCategory());

        });
    }

    @Test
    void Get_GetDiscountListNotFound_Ok() {
        List<DiscountEntity> discounts = discountService.getDiscounts(agreementEntity.getId());
        Assertions.assertNotNull(discounts);
        Assertions.assertTrue(discounts.isEmpty());
    }

    @Test
    void GetById_GetDiscountById_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DiscountEntity dbDiscount = discountService.getDiscountById(agreementEntity.getId(), discountEntity.getId());
        Assertions.assertNotNull(dbDiscount);
        Assertions.assertEquals(discountEntity.getId(), dbDiscount.getId());
    }

    @Test
    void Update_UpdateDiscountWithValidData_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntityWithoutProduct(agreementEntity);

        updatedDiscount.setName("updated_name");
        updatedDiscount.setDescription("updated_description");
        updatedDiscount.setStartDate(LocalDate.now().plusDays(1));
        updatedDiscount.setEndDate(LocalDate.now().plusMonths(3));
        updatedDiscount.setDiscountValue(40);
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setProductCategory(ProductCategoryEnum.ARTS);
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
    void Update_UpdateDiscountWithRequiredFieldToNull_ThrowException() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        discountEntity.setDescription(null);
        Assertions.assertThrows(Exception.class, () -> {
            discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), discountEntity);
        });
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
    void Publish_PublishDiscountWithStartDateAfterToday_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setStartDate(LocalDate.now().plusDays(2));
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        Assertions.assertThrows(InvalidRequestException.class, () -> discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId()));
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.DRAFT, dbDiscount.getState());
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_PublishDiscountWithEndDateBeforeToday_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setStartDate(LocalDate.now().minusDays(20));
        discountEntity.setEndDate(LocalDate.now().minusDays(2));
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        Assertions.assertThrows(InvalidRequestException.class, () -> discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId()));
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.DRAFT, dbDiscount.getState());
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_PublishDiscountWithAgreementStartDateAfterToday_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        agreementEntity.setStartDate(LocalDate.now().plusDays(2));
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        approveAgreement();  //simulation of approved
        agreementEntity.setStartDate(LocalDate.now().plusDays(1));
        agreementEntity = agreementRepository.save(agreementEntity);
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());
        //publish discount
        Assertions.assertThrows(InvalidRequestException.class, () -> discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId()));
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(DiscountStateEnum.DRAFT, dbDiscount.getState());
        Assertions.assertNull(agreementEntity.getFirstDiscountPublishingDate());

    }

    @Test
    void Publish_FirstDiscountPublishDateNotUpdatedIfDiscountWasPublished_Ok() {
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
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        DiscountEntity dbDiscount = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());

        Assertions.assertThrows(InvalidRequestException.class,
                () ->discountService.publishDiscount(agreementEntity.getId(), dbDiscount.getId()));

        List<DiscountEntity> discounts = discountService.getDiscounts(agreementEntity.getId());
        Assertions.assertNotNull(discounts);
        Assertions.assertFalse(discounts.isEmpty());
        discountEntity = discounts.stream().filter(d -> d.getId().equals(dbDiscount.getId())).findFirst().orElseThrow();
        Assertions.assertEquals(DiscountStateEnum.DRAFT, discountEntity.getState());

    }

    @Test
    void Publish_PublishApprovedAgreement_UpdateLastModifyDate() {
        //AgreementEntity agreement = createPendingAgreement();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementEntity = agreementRepository.save(agreementEntity);
        agreementEntity = backofficeAgreementService.approveAgreement(agreementEntity.getId());

        discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        agreementEntity = agreementRepository.findById(agreementEntity.getId()).orElseThrow();
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getInformationLastUpdateDate());

    }

    @Test
    void Update_UpdateApprovedAgreement_UpdateLastModifyDate() {
        setAdminAuth();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        agreementEntity = agreementService.requestApproval(agreementEntity.getId());
        agreementEntity.setBackofficeAssignee(CGNUtils.getJwtAdminUserName());
        agreementEntity = agreementRepository.save(agreementEntity);
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
        productEntity.setProductCategory(ProductCategoryEnum.ARTS);
        toUpdateDiscountEntity.setProducts(Collections.singletonList(productEntity));
        toUpdateDiscountEntity.setDiscountValue(70);
        discountEntity = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), toUpdateDiscountEntity);

        Assertions.assertEquals(70, discountEntity.getDiscountValue());
        agreementEntity = agreementRepository.findById(agreementEntity.getId()).orElseThrow();
        Assertions.assertEquals(LocalDate.now(), agreementEntity.getInformationLastUpdateDate());

    }




    private void approveAgreement() {
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementEntity.setStartDate(LocalDate.now());
        agreementEntity.setEndDate(CGNUtils.getDefaultAgreementEndDate());
    }
}

