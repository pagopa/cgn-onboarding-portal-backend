package it.gov.pagopa.cgn.portal.converter.discount;

import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.model.UpdateDiscount;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UpdateDiscountConverter
        extends CommonDiscountConverter<DiscountEntity, UpdateDiscount> {

    @Override
    protected Function<DiscountEntity, UpdateDiscount> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<UpdateDiscount, DiscountEntity> toEntityFunction() {
        return toEntity;
    }

    protected Function<DiscountEntity, UpdateDiscount> toDto = entity -> {
        throw new UnsupportedOperationException("Not implemented yet");
    };

    protected Function<UpdateDiscount, DiscountEntity> toEntity = updateDiscountDTO -> {
        DiscountEntity entity = new DiscountEntity();
        entity.setDescription(updateDiscountDTO.getDescription());
        entity.setDescriptionEn(updateDiscountDTO.getDescriptionEn());
        entity.setDescriptionDe(updateDiscountDTO.getDescriptionDe());
        entity.setName(updateDiscountDTO.getName());
        entity.setNameEn(updateDiscountDTO.getNameEn());
        entity.setNameDe(updateDiscountDTO.getNameDe());
        entity.setStartDate(updateDiscountDTO.getStartDate());
        entity.setEndDate(updateDiscountDTO.getEndDate());
        entity.setDiscountValue(updateDiscountDTO.getDiscount());
        entity.setStaticCode(updateDiscountDTO.getStaticCode());
        entity.setVisibleOnEyca(updateDiscountDTO.getVisibleOnEyca());
        entity.setLandingPageUrl(updateDiscountDTO.getLandingPageUrl());
        entity.setEycaLandingPageUrl(updateDiscountDTO.getEycaLandingPageUrl());
        entity.setLandingPageReferrer(updateDiscountDTO.getLandingPageReferrer());
        entity.setCondition(updateDiscountDTO.getCondition());
        entity.setConditionEn(updateDiscountDTO.getConditionEn());
        entity.setConditionDe(updateDiscountDTO.getConditionDe());
        entity.setProducts(toEntityDiscountProduct.apply(updateDiscountDTO.getProductCategories(), entity));
        entity.setLastBucketCodeLoadUid(updateDiscountDTO.getLastBucketCodeLoadUid());
        entity.setLastBucketCodeLoadFileName(updateDiscountDTO.getLastBucketCodeLoadFileName());
        entity.setDiscountUrl(updateDiscountDTO.getDiscountUrl());
        return entity;
    };

}
