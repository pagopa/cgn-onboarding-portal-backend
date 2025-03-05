package it.gov.pagopa.cgn.portal.converter.backoffice.approved;

import it.gov.pagopa.cgn.portal.converter.backoffice.CommonBackofficeDiscountConverter;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreementDiscount;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.DiscountState;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ProductCategory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class BackofficeApprovedDiscountConverter
        extends CommonBackofficeDiscountConverter<DiscountEntity, ApprovedAgreementDiscount> {


    private static final Map<ProductCategoryEnum, ProductCategory> productEnumMaps = new EnumMap<>(ProductCategoryEnum.class);

    static {
        productEnumMaps.put(ProductCategoryEnum.BANKING_SERVICES, ProductCategory.BANKINGSERVICES);
        productEnumMaps.put(ProductCategoryEnum.CULTURE_AND_ENTERTAINMENT, ProductCategory.CULTUREANDENTERTAINMENT);
        productEnumMaps.put(ProductCategoryEnum.HEALTH, ProductCategory.HEALTH);
        productEnumMaps.put(ProductCategoryEnum.HOME, ProductCategory.HOME);
        productEnumMaps.put(ProductCategoryEnum.JOB_OFFERS, ProductCategory.JOBOFFERS);
        productEnumMaps.put(ProductCategoryEnum.LEARNING, ProductCategory.LEARNING);
        productEnumMaps.put(ProductCategoryEnum.SPORTS, ProductCategory.SPORTS);
        productEnumMaps.put(ProductCategoryEnum.SUSTAINABLE_MOBILITY, ProductCategory.SUSTAINABLEMOBILITY);
        productEnumMaps.put(ProductCategoryEnum.TELEPHONY_AND_INTERNET, ProductCategory.TELEPHONYANDINTERNET);
        productEnumMaps.put(ProductCategoryEnum.TRAVELLING, ProductCategory.TRAVELLING);
    }

    protected Function<ProductCategoryEnum, ProductCategory> toProductDtoEnum = productCategoryEnum -> Optional.ofNullable(
                                                                                                                       productEnumMaps.get(productCategoryEnum))
                                                                                                               .orElseThrow(
                                                                                                                       () -> getInvalidEnumMapping(
                                                                                                                               productCategoryEnum.name()));
    protected Function<List<DiscountProductEntity>, List<ProductCategory>> toProductDtoListEnum = discountProductsEntity -> discountProductsEntity.stream()
                                                                                                                                                  .map(discountProductEntity -> toProductDtoEnum.apply(
                                                                                                                                                          discountProductEntity.getProductCategory()))
                                                                                                                                                  .collect(
                                                                                                                                                          Collectors.toList());
    protected Function<DiscountEntity, ApprovedAgreementDiscount> toDto = entity -> {
        ApprovedAgreementDiscount dto = new ApprovedAgreementDiscount();
        dto.setId(String.valueOf(entity.getId()));
        dto.setDiscount(entity.getDiscountValue());
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setNameDe(entity.getNameDe());
        dto.setCondition(entity.getCondition());
        dto.setConditionEn(entity.getConditionEn());
        dto.setConditionDe(entity.getConditionDe());
        dto.setDescription(entity.getDescription());
        dto.setDescriptionEn(entity.getDescriptionEn());
        dto.setDescriptionDe(entity.getDescriptionDe());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setDiscountUrl(entity.getDiscountUrl());
        dto.setVisibleOnEyca(entity.getVisibleOnEyca());

        OffsetDateTime updateDateTime;
        updateDateTime = entity.getUpdateTime()!=null ? entity.getUpdateTime():entity.getInsertTime();
        dto.setLastUpateDate(LocalDate.from(updateDateTime));
        dto.setProductCategories(toProductDtoListEnum.apply(entity.getProducts()));
        dto.setState(toDtoEnum.apply(entity.getState(), entity.getEndDate()));

        dto.setTestFailureReason(entity.getTestFailureReason());
        dto.setStaticCode(entity.getStaticCode());
        dto.setLandingPageUrl(entity.getLandingPageUrl());
        dto.setLandingPageReferrer(entity.getLandingPageReferrer());
        dto.setEycaLandingPageUrl(entity.getEycaLandingPageUrl());

        return dto;
    };

    @Override
    protected Function<DiscountEntity, ApprovedAgreementDiscount> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<ApprovedAgreementDiscount, DiscountEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
