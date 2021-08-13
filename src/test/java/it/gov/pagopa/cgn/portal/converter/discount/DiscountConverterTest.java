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
        DiscountEntity discountEntity = createSampleDiscountEntityWithStaticCode(STATIC_CODE);
        Discount dto = discountConverter.toDto(discountEntity);

        checkCommonDiscountEntityToDTOAssertions(discountEntity, dto);
        Assert.assertEquals(STATIC_CODE, dto.getStaticCode());
        Assert.assertNull(dto.getLandingPageUrl());
        Assert.assertNull(dto.getLandingPageReferrer());
    }

    @Test
    public void Convert_ConvertDiscountEntityWithLandingPageDiscountTypeToDTO_Ok() {
        DiscountEntity discountEntity = createSampleDiscountEntityWithLandingPage(URL, REFERRER);
        Discount dto = discountConverter.toDto(discountEntity);

        checkCommonDiscountEntityToDTOAssertions(discountEntity, dto);
        Assert.assertNull(dto.getStaticCode());
        Assert.assertEquals(URL, dto.getLandingPageUrl());
        Assert.assertEquals(REFERRER, dto.getLandingPageReferrer());
    }

    private DiscountEntity createSampleDiscountEntityWithStaticCode(String staticCode) {
        DiscountEntity discountEntity = createSampleCommonDiscountEntity();
        discountEntity.setStaticCode(staticCode);
        return discountEntity;
    }

    private DiscountEntity createSampleDiscountEntityWithLandingPage(String url, String referrer) {
        DiscountEntity discountEntity = createSampleCommonDiscountEntity();
        discountEntity.setLandingPageUrl(url);
        discountEntity.setLandingPageReferrer(referrer);
        return discountEntity;
    }

    private DiscountEntity createSampleCommonDiscountEntity() {
        DiscountEntity discountEntity = new DiscountEntity();
        discountEntity.setName("Discount");
        discountEntity.setStartDate(LocalDate.now());
        discountEntity.setEndDate(LocalDate.now());
        discountEntity.setDiscountValue(50);
        discountEntity.setDescription("A discount");
        discountEntity.setState(DiscountStateEnum.DRAFT);
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        discountEntity.setAgreement(agreementEntity);
        ArrayList<DiscountProductEntity> products = new ArrayList<DiscountProductEntity>();
        discountEntity.setProducts(products);
        discountEntity.setSuspendedReasonMessage("A reason");
        return discountEntity;
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
