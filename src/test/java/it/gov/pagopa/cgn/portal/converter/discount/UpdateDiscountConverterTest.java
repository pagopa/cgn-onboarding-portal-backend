package it.gov.pagopa.cgn.portal.converter.discount;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.model.UpdateDiscount;

@RunWith(SpringRunner.class)
public class UpdateDiscountConverterTest {

    private static final String STATIC_CODE = "static_code";
    private static final String URL = "www.landingpage.com";
    private static final String REFERRER = "referrer";
    private final UpdateDiscountConverter updateDiscountConverter = new UpdateDiscountConverter();

    @Test
    public void Convert_ConvertDiscountWithStaticDiscountTypeDTOToEntity_Ok() {
        UpdateDiscount dto = createSampleDiscountWithStaticCode(STATIC_CODE);
        DiscountEntity discountEntity = updateDiscountConverter.toEntity(dto);

        checkCommonCreateDiscountAssertions(dto, discountEntity);
        Assert.assertEquals(STATIC_CODE, discountEntity.getStaticCode());
    }

    @Test
    public void Convert_ConvertDiscountWithLandingPageDiscountTypeDTOToEntity_Ok() {
        UpdateDiscount dto = createSampleDiscountWithLandingPage(URL, REFERRER);
        DiscountEntity discountEntity = updateDiscountConverter.toEntity(dto);

        checkCommonCreateDiscountAssertions(dto, discountEntity);
        Assert.assertEquals(URL, discountEntity.getLandingPageUrl());
        Assert.assertEquals(REFERRER, discountEntity.getLandingPageReferrer());
    }

    private UpdateDiscount createSampleDiscountWithStaticCode(String staticCode) {
        UpdateDiscount dto = createSampleCommonDiscount();
        dto.setStaticCode(staticCode);
        return dto;
    }

    private UpdateDiscount createSampleDiscountWithLandingPage(String url, String referrer) {
        UpdateDiscount dto = createSampleCommonDiscount();
        dto.setLandingPageUrl(url);
        dto.setLandingPageReferrer(referrer);
        return dto;
    }

    private UpdateDiscount createSampleCommonDiscount() {
        UpdateDiscount dto = new UpdateDiscount();
        dto.setName("Discount");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());
        dto.setDiscount(50);
        dto.setDescription("A discount");
        return dto;
    }

    private void checkCommonCreateDiscountAssertions(UpdateDiscount dto, DiscountEntity discountEntity) {
        Assert.assertEquals(dto.getName(), discountEntity.getName());
        Assert.assertEquals(dto.getDescription(), discountEntity.getDescription());
        Assert.assertEquals(dto.getStartDate(), discountEntity.getStartDate());
        Assert.assertEquals(dto.getEndDate(), discountEntity.getEndDate());
        Assert.assertEquals(dto.getDiscount(), discountEntity.getDiscountValue());
    }

}
