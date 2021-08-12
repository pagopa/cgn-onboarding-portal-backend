package it.gov.pagopa.cgn.portal.converter.discount;


import it.gov.pagopa.cgnonboardingportal.model.CreateDiscount;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class CreateDiscountConverter extends CommonDiscountConverter<DiscountEntity, CreateDiscount> {

    @Override
    protected Function<DiscountEntity, CreateDiscount> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<CreateDiscount, DiscountEntity> toEntityFunction() {
        return toEntity;
    }


    protected Function<DiscountEntity, CreateDiscount> toDto =
            entity -> {
               throw new UnsupportedOperationException("Not implemented yet");
            };

    protected Function<CreateDiscount, DiscountEntity> toEntity =
            dto -> {
                DiscountEntity entity = new DiscountEntity();
                entity.setDescription(dto.getDescription());
                entity.setName(dto.getName());
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(dto.getEndDate());
                entity.setDiscountValue(dto.getDiscount());
                entity.setStaticCode(dto.getStaticCode());
                entity.setLandingPageUrl(dto.getLandingPageUrl());
                entity.setLandingPageReferrer(dto.getLandingPageReferrer());
                entity.setCondition(dto.getCondition());
                entity.setProducts(toEntityDiscountProduct.apply(dto.getProductCategories(), entity));
                entity.setState(DiscountStateEnum.DRAFT); //default state
                return entity;
            };

}