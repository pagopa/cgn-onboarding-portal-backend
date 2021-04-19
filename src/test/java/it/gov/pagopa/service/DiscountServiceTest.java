package it.gov.pagopa.service;

import it.gov.pagopa.BaseTest;
import it.gov.pagopa.enums.DiscountStateEnum;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.DiscountEntity;
import it.gov.pagopa.model.DiscountProductEntity;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class DiscountServiceTest extends BaseTest {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private SessionFactory sessionFactory;

    private AgreementEntity agreementEntity;


    @BeforeEach
    void beforeEach() {
        agreementEntity = agreementService.createAgreementIfNotExists();
    }

    @Test
    void Create_CreateDiscountWithValidData_Ok() {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreementEntity);
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
        DiscountEntity discountEntity = createSampleDiscountEntity(agreementEntity);
        discountEntity.setProducts(null);
        Assertions.assertThrows(Exception.class, ()-> {
            discountService.createDiscount(agreementEntity.getId(), discountEntity);
            sessionFactory.getCurrentSession().flush();
        });
    }

    @Test
    void Get_GetDiscountList_Ok() {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreementEntity);
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
            Assertions.assertEquals(discountEntity.getProducts().get(idx), discountDB.getProducts().get(idx));
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
        DiscountEntity discountEntity = createSampleDiscountEntity(agreementEntity);
        discountEntity = discountService.createDiscount(agreementEntity.getId(), discountEntity);
        discountEntity.setName("updated_name");
        discountEntity.setDescription("updated_description");
        discountEntity.setStartDate(LocalDate.now().plusDays(1));
        discountEntity.setEndDate(LocalDate.now().plusMonths(3));
        discountEntity.setDiscountValue(40.0);
        discountEntity.getProducts().forEach(p->p.setProductCategory(p.getProductCategory() + "_updated"));
        discountEntity.setCondition("update_condition");
        discountEntity.setStaticCode("update_static_code");

        DiscountEntity updatedDiscount;
        updatedDiscount = discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), discountEntity);
        Assertions.assertEquals(discountEntity.getName(), updatedDiscount.getName());
        Assertions.assertEquals(discountEntity.getDescription(), updatedDiscount.getDescription());
        Assertions.assertEquals(discountEntity.getStartDate(), updatedDiscount.getStartDate());
        Assertions.assertEquals(discountEntity.getEndDate(), updatedDiscount.getEndDate());
        Assertions.assertEquals(discountEntity.getDiscountValue(), updatedDiscount.getDiscountValue());
        Assertions.assertEquals(discountEntity.getProducts(), updatedDiscount.getProducts());
        Assertions.assertEquals(discountEntity.getCondition(), updatedDiscount.getCondition());
        Assertions.assertEquals(discountEntity.getStaticCode(), updatedDiscount.getStaticCode());

    }

    @Test
    void Update_UpdateDiscountWithRequiredFieldToNull_ThrowException() {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        discountEntity.setDescription(null);
        Assertions.assertThrows(Exception.class, () -> {
            discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), discountEntity);
            sessionFactory.getCurrentSession().flush();
        });
    }

    @Test
    void Update_UpdateDiscountNotExists_ThrowException() {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreementEntity);
        discountEntity.setDescription(null);
        Assertions.assertThrows(Exception.class,
                () -> discountService.updateDiscount(agreementEntity.getId(), discountEntity.getId(), discountEntity));
    }

    @Test
    void Delete_DeleteDiscount_Ok() {
        DiscountEntity discountEntity = createSampleDiscountEntity(agreementEntity);
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

