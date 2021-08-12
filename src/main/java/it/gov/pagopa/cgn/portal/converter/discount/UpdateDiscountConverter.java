package it.gov.pagopa.cgn.portal.converter.discount;


import it.gov.pagopa.cgnonboardingportal.model.UpdateDiscount;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UpdateDiscountConverter extends CommonDiscountConverter<DiscountEntity, UpdateDiscount> {

    @Override
    protected Function<DiscountEntity, UpdateDiscount> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<UpdateDiscount, DiscountEntity> toEntityFunction() {
        return toEntity;
    }


    protected Function<DiscountEntity, UpdateDiscount> toDto =
            entity -> {
               throw new UnsupportedOperationException("Not implemented yet");
            };


    protected Function<UpdateDiscount, DiscountEntity> toEntity =
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
                return entity;
            };

}