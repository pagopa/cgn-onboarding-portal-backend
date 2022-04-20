package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.converter.discount.UpdateDiscountConverter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.model.UpdateDiscount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UpdateDiscountConverterTest {

    private final UpdateDiscountConverter updateDiscountConverter = new UpdateDiscountConverter();

    @Test
    public void Convert_ConvertUpdateDiscountDTOToUpdateDiscountEntity_Ok() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        UpdateDiscount updateDiscount = TestUtils.updatableDiscountFromDiscountEntity(discountEntity);
        DiscountEntity convertedDiscountEntity = updateDiscountConverter.toEntity(updateDiscount);
        commonAssertionsEntityToDto(convertedDiscountEntity, updateDiscount);
    }

    private void commonAssertionsEntityToDto(DiscountEntity discountEntity, UpdateDiscount updateDiscountDTO) {
        Assert.assertEquals(discountEntity.getDescription(), updateDiscountDTO.getDescription());
        Assert.assertEquals(discountEntity.getName(), updateDiscountDTO.getName());
        Assert.assertEquals(discountEntity.getStartDate(), updateDiscountDTO.getStartDate());
        Assert.assertEquals(discountEntity.getEndDate(), updateDiscountDTO.getEndDate());
        Assert.assertEquals(discountEntity.getDiscountValue(), updateDiscountDTO.getDiscount());
        Assert.assertEquals(discountEntity.getStaticCode(), updateDiscountDTO.getStaticCode());
        Assert.assertEquals(discountEntity.getVisibleOnEyca(), updateDiscountDTO.getVisibleOnEyca());
        Assert.assertEquals(discountEntity.getLandingPageUrl(), updateDiscountDTO.getLandingPageUrl());
        Assert.assertEquals(discountEntity.getLandingPageReferrer(), updateDiscountDTO.getLandingPageReferrer());
        Assert.assertEquals(discountEntity.getLastBucketCodeLoadUid(), updateDiscountDTO.getLastBucketCodeLoadUid());
        Assert.assertEquals(discountEntity.getLastBucketCodeLoadFileName(),
                            updateDiscountDTO.getLastBucketCodeLoadFileName());
    }

}
