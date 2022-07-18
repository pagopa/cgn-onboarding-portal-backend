package it.gov.pagopa.cgn.portal.validator;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CheckDiscountValidatorTest {

    private final CheckDiscountValidator checkDiscountValidator = new CheckDiscountValidator();

    @Test
    public void Test_isValid_AllDescriptions_AllConditions_Valid() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);

        Assertions.assertTrue(checkDiscountValidator.isValid(discountEntity, null));
    }

    @Test
    public void Test_isValid_NoDescriptions_AllConditions_Valid() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);

        discountEntity.setDescription(null);
        discountEntity.setDescriptionEn(null);
        discountEntity.setDescriptionDe(null);

        Assertions.assertTrue(checkDiscountValidator.isValid(discountEntity, null));
    }

    @Test
    public void Test_isValid_NoDescriptions_NoConditions_Valid() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);

        discountEntity.setDescription(null);
        discountEntity.setDescriptionEn(null);
        discountEntity.setDescriptionDe(null);

        discountEntity.setCondition(null);
        discountEntity.setConditionEn(null);
        discountEntity.setConditionDe(null);

        Assertions.assertTrue(checkDiscountValidator.isValid(discountEntity, null));
    }

    @Test
    public void Test_isValid_PartialDescriptions_PartialConditions_NotValid() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);

        discountEntity.setDescription(null);
        discountEntity.setDescriptionEn("null");
        discountEntity.setDescriptionDe(null);

        discountEntity.setCondition(null);
        discountEntity.setConditionEn("null");
        discountEntity.setConditionDe(null);

        Assertions.assertFalse(checkDiscountValidator.isValid(discountEntity, null));
    }
}
