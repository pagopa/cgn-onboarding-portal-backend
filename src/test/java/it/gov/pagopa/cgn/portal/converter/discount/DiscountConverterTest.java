package it.gov.pagopa.cgn.portal.converter.discount;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgnonboardingportal.model.Discount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.ArrayList;

@RunWith(SpringRunner.class)
public class DiscountConverterTest {

    private static final String STATIC_CODE = "static_code";
    private static final String URL = "www.landingpage.com";
    private static final String REFERRER = "referrer";
    private final DiscountConverter discountConverter = new DiscountConverter();

    @Test
    public void Convert_ConvertDiscountDtoToEntity_ThrowUnsupportedException() {
        Discount discount = new Discount();

        //Not implemented yet
        Assert.assertThrows(UnsupportedOperationException.class, () -> discountConverter.toEntity(discount));
    }

    @Test
    public void Convert_ConvertDiscountEntityWithStaticDiscountTypeToDTO_Ok() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithStaticCode(agreementEntity, STATIC_CODE);
        Discount dto = discountConverter.toDto(discountEntity);

        checkCommonDiscountEntityToDTOAssertions(discountEntity, dto);
        Assert.assertEquals(STATIC_CODE, dto.getStaticCode());
        Assert.assertNull(dto.getLandingPageUrl());
        Assert.assertNull(dto.getLandingPageReferrer());
    }

    @Test
    public void Convert_ConvertDiscountEntityWithLandingPageDiscountTypeToDTO_Ok() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity, URL, REFERRER);
        Discount dto = discountConverter.toDto(discountEntity);

        checkCommonDiscountEntityToDTOAssertions(discountEntity, dto);
        Assert.assertNull(dto.getStaticCode());
        Assert.assertEquals(URL, dto.getLandingPageUrl());
        Assert.assertEquals(REFERRER, dto.getLandingPageReferrer());
    }


    private void checkCommonDiscountEntityToDTOAssertions(DiscountEntity discountEntity, Discount dto) {
        Assert.assertEquals(dto.getName(), discountEntity.getName());
        Assert.assertEquals(dto.getDescription(), discountEntity.getDescription());
        Assert.assertEquals(dto.getStartDate(), discountEntity.getStartDate());
        Assert.assertEquals(dto.getEndDate(), discountEntity.getEndDate());
        Assert.assertEquals(dto.getDiscount(), discountEntity.getDiscountValue());
        Assert.assertEquals(DiscountStateEnum.DRAFT, discountEntity.getState());
    }

}
