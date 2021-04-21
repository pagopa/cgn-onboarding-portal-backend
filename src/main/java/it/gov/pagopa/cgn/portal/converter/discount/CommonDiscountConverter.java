package it.gov.pagopa.cgn.portal.converter.discount;

import it.gov.pagopa.cgnonboardingportal.model.DiscountState;
import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CommonDiscountConverter<E, D> extends AbstractConverter<E, D> {

    private static final Map<DiscountStateEnum, DiscountState> enumMap = new EnumMap<>(DiscountStateEnum.class);
    static {
        enumMap.put(DiscountStateEnum.DRAFT, DiscountState.DRAFT);
        enumMap.put(DiscountStateEnum.PUBLISHED, DiscountState.PUBLISHED);
        enumMap.put(DiscountStateEnum.REJECTED, DiscountState.REJECTED);
    }

    protected Function<List<DiscountProductEntity>, List<String>> toProductDto =
            productEntityList -> productEntityList.stream()
                    .map(DiscountProductEntity::getProductCategory).collect(Collectors.toList());

    protected Function<DiscountStateEnum, DiscountState> toDtoEnum = entityEnum ->
            Optional.ofNullable(enumMap.get(entityEnum))
                    .orElseThrow(() -> new InvalidRequestException("Enum mapping not found for " + entityEnum));

    protected BiFunction<List<String>, DiscountEntity, List<DiscountProductEntity>> toEntityDiscountProduct =
            (productDtoList, discount) ->
                    productDtoList.stream().map(productCategory -> {
                        DiscountProductEntity productEntity = new DiscountProductEntity();
                        productEntity.setDiscount(discount);
                        productEntity.setProductCategory(productCategory);
                        return productEntity;
                    }).collect(Collectors.toList());
}
