package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.converter.backoffice.approved.BackofficeApprovedDiscountConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreementDiscount;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.DiscountState;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
public class BackofficeApprovedDiscountConverterTest {

    @Test
    public void ToDto_TestPendingDiscountToDto_ok() {
        BackofficeApprovedDiscountConverter converter = getBackofficeAgreementConverter();
        AgreementEntity agreement = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discount = TestUtils.createSampleDiscountEntity(agreement);
        discount.setStaticCode("acode");
        discount.setState(DiscountStateEnum.TEST_PENDING);

        ApprovedAgreementDiscount discountDto = converter.toDto(discount);
        Assert.assertNotNull(discountDto);
        Assert.assertEquals(DiscountState.TEST_PENDING, discountDto.getState());
        Assert.assertNotNull(discountDto.getStaticCode());
    }

    @Test
    public void ToDto_PublishedDiscountToDto_ok() {
        BackofficeApprovedDiscountConverter converter = getBackofficeAgreementConverter();
        AgreementEntity agreement = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discount = TestUtils.createSampleDiscountEntity(agreement);
        discount.setStaticCode("acode");
        discount.setState(DiscountStateEnum.PUBLISHED);

        ApprovedAgreementDiscount discountDto = converter.toDto(discount);
        Assert.assertNotNull(discountDto);
        Assert.assertEquals(DiscountState.PUBLISHED, discountDto.getState());
        Assert.assertNull(discountDto.getStaticCode());
    }

    @Test
    public void ToDto_PublishedDiscountToDtoWithCurrentEndDate_ok() {
        BackofficeApprovedDiscountConverter converter = getBackofficeAgreementConverter();
        AgreementEntity agreement = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discount = TestUtils.createSampleDiscountEntity(agreement);
        discount.setEndDate(LocalDate.now());
        discount.setState(DiscountStateEnum.PUBLISHED);

        ApprovedAgreementDiscount discountDto = converter.toDto(discount);
        Assert.assertNotNull(discountDto);
        Assert.assertEquals(DiscountState.EXPIRED, discountDto.getState());
    }

    private BackofficeApprovedDiscountConverter getBackofficeAgreementConverter() {
        return new BackofficeApprovedDiscountConverter();
    }

}
