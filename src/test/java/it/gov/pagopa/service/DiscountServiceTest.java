package it.gov.pagopa.service;

import it.gov.pagopa.cgn.IntegrationAbstractTest;
import it.gov.pagopa.cgn.TestUtils;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.DiscountEntity;
import it.gov.pagopa.repository.AgreementRepository;
import it.gov.pagopa.repository.AgreementUserRepository;
import it.gov.pagopa.repository.DiscountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("dev")
class DiscountServiceTest extends IntegrationAbstractTest {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private AgreementUserRepository userRepository;

    private AgreementEntity agreementEntity;

    @BeforeEach
    void beforeEach() {
        agreementEntity = agreementService.createAgreementIfNotExists();
    }

    @AfterEach
    void clean() {
        discountRepository.deleteAll();
        agreementRepository.deleteAll();
        userRepository.deleteAll();
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
        Assertions.assertEquals(discountEntity.getAgreement(), agreementEntity);
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
    void Update_UpdateDiscountWithValidData_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        DiscountEntity updatedDiscount = TestUtils.createSampleDiscountEntity(agreementEntity);
        updatedDiscount.setName("updated_name");
        updatedDiscount.setDescription("updated_description");
        updatedDiscount.setStartDate(LocalDate.now().plusDays(1));
        updatedDiscount.setEndDate(LocalDate.now().plusMonths(3));
        updatedDiscount.setDiscountValue(40.0);
        updatedDiscount.getProducts().forEach(p->p.setProductCategory(p.getProductCategory() + "_updated"));
        updatedDiscount.setCondition("update_condition");
        updatedDiscount.setStaticCode("update_static_code");


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
        Assertions.assertEquals(updatedDiscount.getProducts().get(1), dbDiscount.getProducts().get(1));
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

}

