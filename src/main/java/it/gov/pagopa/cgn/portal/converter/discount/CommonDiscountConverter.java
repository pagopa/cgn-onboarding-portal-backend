package it.gov.pagopa.cgn.portal.converter.discount;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgnonboardingportal.model.DiscountState;
import it.gov.pagopa.cgnonboardingportal.model.ProductCategory;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CommonDiscountConverter<E, D> extends AbstractConverter<E, D> {

    private static final Map<DiscountStateEnum, DiscountState> enumMap = new EnumMap<>(DiscountStateEnum.class);
    private static final Map<ProductCategoryEnum, ProductCategory> productEnumMaps = new EnumMap<>(ProductCategoryEnum.class);
    static {
        enumMap.put(DiscountStateEnum.DRAFT, DiscountState.DRAFT);
        enumMap.put(DiscountStateEnum.PUBLISHED, DiscountState.PUBLISHED);
        enumMap.put(DiscountStateEnum.SUSPENDED, DiscountState.SUSPENDED);

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


    protected Function<ProductCategoryEnum, ProductCategory> toProductDtoEnum = productCategoryEnum ->
            Optional.ofNullable(productEnumMaps.get(productCategoryEnum))
                    .orElseThrow(() -> getInvalidEnumMapping(productCategoryEnum.name()));

    protected Function<ProductCategory, ProductCategoryEnum> toProductEntityEnum = productDto ->
            productEnumMaps.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(productDto))
                    .map(Map.Entry::getKey)
                    .findFirst().orElseThrow();

    protected Function<List<DiscountProductEntity>, List<ProductCategory>> toProductDtoListEnum = discountProductsEntity ->
            discountProductsEntity.stream()
                    .map(discountProductEntity -> toProductDtoEnum.apply(discountProductEntity.getProductCategory()))
                    .collect(Collectors.toList());

    protected BiFunction<DiscountStateEnum, LocalDate, DiscountState> toDtoEnum = (entityEnum, endDate) -> {
        if (LocalDate.now().isAfter(endDate)) {
            return DiscountState.EXPIRED;
        }
        return Optional.ofNullable(enumMap.get(entityEnum))
                .orElseThrow(() -> new InvalidRequestException("Enum mapping not found for " + entityEnum));
    };

    protected BiFunction<List<ProductCategory>, DiscountEntity, List<DiscountProductEntity>> toEntityDiscountProduct =
            (productDtoList, discountEntity) ->
                    productDtoList.stream().map(productCategory -> {
                        DiscountProductEntity productEntity = new DiscountProductEntity();
                        productEntity.setDiscount(discountEntity);
                        productEntity.setProductCategory(toProductEntityEnum.apply(productCategory));
                        return productEntity;
                    }).collect(Collectors.toList());
}
