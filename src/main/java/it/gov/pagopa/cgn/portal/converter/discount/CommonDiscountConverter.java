package it.gov.pagopa.cgn.portal.converter.discount;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import it.gov.pagopa.cgnonboardingportal.model.BucketCodeLoadStatus;
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
    private static final Map<ProductCategoryEnum, ProductCategory> productCategoryEnumMaps = new EnumMap<>(
            ProductCategoryEnum.class);
    private static final Map<BucketCodeLoadStatusEnum, BucketCodeLoadStatus> bucketLoadStatusEnumMap = new EnumMap<>(
            BucketCodeLoadStatusEnum.class);

    static {
        enumMap.put(DiscountStateEnum.DRAFT, DiscountState.DRAFT);
        enumMap.put(DiscountStateEnum.PUBLISHED, DiscountState.PUBLISHED);
        enumMap.put(DiscountStateEnum.SUSPENDED, DiscountState.SUSPENDED);
        enumMap.put(DiscountStateEnum.TEST_PENDING, DiscountState.TEST_PENDING);
        enumMap.put(DiscountStateEnum.TEST_PASSED, DiscountState.TEST_PASSED);
        enumMap.put(DiscountStateEnum.TEST_FAILED, DiscountState.TEST_FAILED);

        productCategoryEnumMaps.put(ProductCategoryEnum.BANKING_SERVICES, ProductCategory.BANKING_SERVICES);
        productCategoryEnumMaps.put(ProductCategoryEnum.CULTURE_AND_ENTERTAINMENT,
                                    ProductCategory.CULTURE_AND_ENTERTAINMENT);
        productCategoryEnumMaps.put(ProductCategoryEnum.HEALTH, ProductCategory.HEALTH);
        productCategoryEnumMaps.put(ProductCategoryEnum.HOME, ProductCategory.HOME);
        productCategoryEnumMaps.put(ProductCategoryEnum.JOB_OFFERS, ProductCategory.JOB_OFFERS);
        productCategoryEnumMaps.put(ProductCategoryEnum.LEARNING, ProductCategory.LEARNING);
        productCategoryEnumMaps.put(ProductCategoryEnum.SPORTS, ProductCategory.SPORTS);
        productCategoryEnumMaps.put(ProductCategoryEnum.SUSTAINABLE_MOBILITY, ProductCategory.SUSTAINABLE_MOBILITY);
        productCategoryEnumMaps.put(ProductCategoryEnum.TELEPHONY_AND_INTERNET, ProductCategory.TELEPHONY_AND_INTERNET);
        productCategoryEnumMaps.put(ProductCategoryEnum.TRAVELLING, ProductCategory.TRAVELLING);

        bucketLoadStatusEnumMap.put(BucketCodeLoadStatusEnum.PENDING, BucketCodeLoadStatus.PENDING);
        bucketLoadStatusEnumMap.put(BucketCodeLoadStatusEnum.RUNNING, BucketCodeLoadStatus.RUNNING);
        bucketLoadStatusEnumMap.put(BucketCodeLoadStatusEnum.FAILED, BucketCodeLoadStatus.FAILED);
        bucketLoadStatusEnumMap.put(BucketCodeLoadStatusEnum.FINISHED, BucketCodeLoadStatus.FINISHED);
    }

    protected Function<ProductCategoryEnum, ProductCategory> toProductDtoEnum
            = productCategoryEnum -> Optional.ofNullable(productCategoryEnumMaps.get(productCategoryEnum))
                                             .orElseThrow(() -> getInvalidEnumMapping(productCategoryEnum.name()));

    protected Function<ProductCategory, ProductCategoryEnum> toProductEntityEnum
            = productDto -> productCategoryEnumMaps.entrySet()
                                                   .stream()
                                                   .filter(entry -> entry.getValue().equals(productDto))
                                                   .map(Map.Entry::getKey)
                                                   .findFirst()
                                                   .orElseThrow();

    protected Function<List<DiscountProductEntity>, List<ProductCategory>> toProductDtoListEnum
            = discountProductsEntity -> discountProductsEntity.stream()
                                                              .map(discountProductEntity -> toProductDtoEnum.apply(
                                                                      discountProductEntity.getProductCategory()))
                                                              .collect(Collectors.toList());

    protected Function<BucketCodeLoadStatusEnum, BucketCodeLoadStatus> toBucketCodeLoadStatusDtoEnum
            = bucketCodeLoadStatusEnum -> Optional.ofNullable(bucketLoadStatusEnumMap.get(bucketCodeLoadStatusEnum))
                                                  .orElseThrow(() -> getInvalidEnumMapping(bucketCodeLoadStatusEnum.name()));

    protected BiFunction<DiscountStateEnum, LocalDate, DiscountState> toDtoEnum = (entityEnum, endDate) -> {
        if (LocalDate.now().isAfter(endDate)) {
            return DiscountState.EXPIRED;
        }
        return Optional.ofNullable(enumMap.get(entityEnum))
                       .orElseThrow(() -> new InvalidRequestException("Enum mapping not found for " + entityEnum));
    };

    protected BiFunction<List<ProductCategory>, DiscountEntity, List<DiscountProductEntity>> toEntityDiscountProduct
            = (productDtoList, discountEntity) -> productDtoList.stream().map(productCategory -> {
        DiscountProductEntity productEntity = new DiscountProductEntity();
        productEntity.setDiscount(discountEntity);
        productEntity.setProductCategory(toProductEntityEnum.apply(productCategory));
        return productEntity;
    }).collect(Collectors.toList());
}
