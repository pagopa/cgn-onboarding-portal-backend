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
            updateDiscountDTO -> {
                DiscountEntity entity = new DiscountEntity();
                entity.setDescription(updateDiscountDTO.getDescription());
                entity.setName(updateDiscountDTO.getName());
                entity.setStartDate(updateDiscountDTO.getStartDate());
                entity.setEndDate(updateDiscountDTO.getEndDate());
                entity.setDiscountValue(updateDiscountDTO.getDiscount());
                entity.setStaticCode(updateDiscountDTO.getStaticCode());
                entity.setLandingPageUrl(updateDiscountDTO.getLandingPageUrl());
                entity.setLandingPageReferrer(updateDiscountDTO.getLandingPageReferrer());
                entity.setCondition(updateDiscountDTO.getCondition());
                entity.setProducts(toEntityDiscountProduct.apply(updateDiscountDTO.getProductCategories(), entity));
                return entity;
            };

}
