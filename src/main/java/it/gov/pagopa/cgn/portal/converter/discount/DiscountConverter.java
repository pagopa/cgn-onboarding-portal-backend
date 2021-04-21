package it.gov.pagopa.cgn.portal.converter.discount;


import it.gov.pagopa.cgnonboardingportal.model.Discount;
import it.gov.pagopa.cgnonboardingportal.model.Discounts;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DiscountProductEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DiscountConverter extends CommonDiscountConverter<DiscountEntity, Discount> {

    @Override
    protected Function<DiscountEntity, Discount> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Discount, DiscountEntity> toEntityFunction() {
        return toEntity;
    }

    public Discounts getDiscountsDtoFromDiscountEntityList(List<DiscountEntity> discountEntityList) {
        return toDiscountsDto.apply(discountEntityList);
    }

    protected Function<List<DiscountEntity>, Discounts> toDiscountsDto = discountEntities -> {
        List<Discount> discountList = CollectionUtils.isEmpty(discountEntities) ?
                Collections.emptyList() : discountEntities.stream().map(toDtoFunction()).collect(Collectors.toList());
        Discounts discounts = new Discounts();
        discounts.setItems(discountList);
        return discounts;
    };

    protected Function<DiscountEntity, Discount> toDto =
            entity -> {
                Discount dto = new Discount();
                dto.setId(String.valueOf(entity.getId()));
                dto.setName(entity.getName());
                dto.setDescription(entity.getDescription());
                dto.setDiscount(entity.getDiscountValue());
                dto.setAgreementId(entity.getAgreement().getId());
                dto.setCondition(entity.getCondition());
                dto.setStartDate(entity.getStartDate());
                dto.setEndDate(entity.getEndDate());
                dto.setStaticCode(entity.getStaticCode());
                dto.setState(toDtoEnum.apply(entity.getState()));
                dto.setProductCategories(toProductDto.apply(entity.getProducts()));
                return dto;
            };

    protected BiFunction<List<String>, DiscountEntity, List<DiscountProductEntity>> toEntityDiscountProduct =
            (productDtoList, discount) ->
                    productDtoList.stream().map(productCategory -> {
                        DiscountProductEntity productEntity = new DiscountProductEntity();
                        productEntity.setDiscount(discount);
                        productEntity.setProductCategory(productCategory);
                        return productEntity;
                    }).collect(Collectors.toList());

    protected Function<Discount, DiscountEntity> toEntity =
            dto -> {
                throw new UnsupportedOperationException("Not implemented yet");
            };

}