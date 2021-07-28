package it.gov.pagopa.cgn.portal.converter.backoffice.approved;

import it.gov.pagopa.cgn.portal.converter.backoffice.CommonBackofficeDiscountConverter;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreementDiscount;
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

@Component
public class BackofficeApprovedDiscountConverter extends CommonBackofficeDiscountConverter<DiscountEntity, ApprovedAgreementDiscount> {


    private static final Map<ProductCategoryEnum, ProductCategory> productEnumMaps = new EnumMap<>(ProductCategoryEnum.class);
    static {
        productEnumMaps.put(ProductCategoryEnum.ENTERTAINMENT, ProductCategory.ENTERTAINMENT);
        productEnumMaps.put(ProductCategoryEnum.TRAVELLING, ProductCategory.TRAVELLING);
        productEnumMaps.put(ProductCategoryEnum.FOOD_DRINK, ProductCategory.FOODDRINK);
        productEnumMaps.put(ProductCategoryEnum.SERVICES, ProductCategory.SERVICES);
        productEnumMaps.put(ProductCategoryEnum.LEARNING, ProductCategory.LEARNING);
        productEnumMaps.put(ProductCategoryEnum.HOTELS, ProductCategory.HOTELS);
        productEnumMaps.put(ProductCategoryEnum.SPORTS, ProductCategory.SPORTS);
        productEnumMaps.put(ProductCategoryEnum.HEALTH, ProductCategory.HEALTH);
        productEnumMaps.put(ProductCategoryEnum.SHOPPING, ProductCategory.SHOPPING);
    }
    @Override
    protected Function<DiscountEntity, ApprovedAgreementDiscount> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<ApprovedAgreementDiscount, DiscountEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected Function<ProductCategoryEnum, ProductCategory> toProductDtoEnum = productCategoryEnum ->
            Optional.ofNullable(productEnumMaps.get(productCategoryEnum))
                    .orElseThrow(() -> getInvalidEnumMapping(productCategoryEnum.name()));

    protected Function<List<DiscountProductEntity>, List<ProductCategory>> toProductDtoListEnum = discountProductsEntity ->
            discountProductsEntity.stream()
                    .map(discountProductEntity -> toProductDtoEnum.apply(discountProductEntity.getProductCategory()))
                    .collect(Collectors.toList());

    protected Function<DiscountEntity, ApprovedAgreementDiscount> toDto =
            entity -> {
                ApprovedAgreementDiscount dto = new ApprovedAgreementDiscount();
                dto.setId(String.valueOf(entity.getId()));
                dto.setDiscount(entity.getDiscountValue());
                dto.setName(entity.getName());
                dto.setCondition(entity.getCondition());
                dto.setDescription(entity.getDescription());
                dto.setStartDate(entity.getStartDate());
                dto.setEndDate(entity.getEndDate());

                OffsetDateTime updateDateTime;
                updateDateTime = entity.getUpdateTime() != null ? entity.getUpdateTime() : entity.getInsertTime();
                dto.setLastUpateDate(LocalDate.from(updateDateTime));
                dto.setProductCategories(toProductDtoListEnum.apply(entity.getProducts()));
                dto.setState(toDtoEnum.apply(entity.getState(), entity.getEndDate()));
                return dto;
            };
}
