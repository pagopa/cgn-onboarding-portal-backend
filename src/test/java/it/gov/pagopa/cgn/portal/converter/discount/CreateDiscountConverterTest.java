package it.gov.pagopa.cgn.portal.converter.discount;

import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.model.CreateDiscount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
public class CreateDiscountConverterTest {

    private static final String STATIC_CODE = "static_code";
    private static final String URL = "https://www.landingpage.com";
    private static final String REFERRER = "referrer";
    private final CreateDiscountConverter createDiscountConverter = new CreateDiscountConverter();

    @Test
    public void Convert_ConvertDiscountEntityToDTO_ThrowUnsupportedException() {
        DiscountEntity discountEntity = new DiscountEntity();
        discountEntity.setName("Discount");

        // Not implemented yet
        Assert.assertThrows(UnsupportedOperationException.class, () -> createDiscountConverter.toDto(discountEntity));
    }

    @Test
    public void Convert_ConvertDiscountWithStaticDiscountTypeDTOToEntity_Ok() {
        CreateDiscount dto = createSampleDiscountWithStaticCode(STATIC_CODE);
        DiscountEntity discountEntity = createDiscountConverter.toEntity(dto);

        checkCommonCreateDiscountAssertions(dto, discountEntity);
        Assert.assertEquals(STATIC_CODE, discountEntity.getStaticCode());
    }

    @Test
    public void Convert_ConvertDiscountWithLandingPageDiscountTypeDTOToEntity_Ok() {
        CreateDiscount dto = createSampleDiscountWithLandingPage(URL, REFERRER);
        DiscountEntity discountEntity = createDiscountConverter.toEntity(dto);

        checkCommonCreateDiscountAssertions(dto, discountEntity);
        Assert.assertEquals(URL, discountEntity.getLandingPageUrl());
        Assert.assertEquals(REFERRER, discountEntity.getLandingPageReferrer());
    }

    private CreateDiscount createSampleDiscountWithStaticCode(String staticCode) {
        CreateDiscount dto = createSampleCommonDiscount();
        dto.setStaticCode(staticCode);
        return dto;
    }

    private CreateDiscount createSampleDiscountWithLandingPage(String url, String referrer) {
        CreateDiscount dto = createSampleCommonDiscount();
        dto.setLandingPageUrl(url);
        dto.setLandingPageReferrer(referrer);
        return dto;
    }

    private CreateDiscount createSampleCommonDiscount() {
        CreateDiscount dto = new CreateDiscount();
        dto.setName("Discount");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());
        dto.setDiscount(50);
        dto.setDescription("A discount");
        return dto;
    }

    private void checkCommonCreateDiscountAssertions(CreateDiscount dto, DiscountEntity discountEntity) {
        Assert.assertEquals(dto.getName(), discountEntity.getName());
        Assert.assertEquals(dto.getDescription(), discountEntity.getDescription());
        Assert.assertEquals(dto.getStartDate(), discountEntity.getStartDate());
        Assert.assertEquals(dto.getEndDate(), discountEntity.getEndDate());
        Assert.assertEquals(dto.getDiscount(), discountEntity.getDiscountValue());
        Assert.assertEquals(DiscountStateEnum.DRAFT, discountEntity.getState());
    }

}
