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

    protected Function<DiscountEntity, CreateDiscount> toDto = entity -> {
        throw new UnsupportedOperationException("Not implemented yet");
    };

    protected Function<CreateDiscount, DiscountEntity> toEntity = createDiscountDTO -> {
        DiscountEntity entity = new DiscountEntity();
        entity.setDescription(createDiscountDTO.getDescription());
        entity.setName(createDiscountDTO.getName());
        entity.setStartDate(createDiscountDTO.getStartDate());
        entity.setEndDate(createDiscountDTO.getEndDate());
        entity.setDiscountValue(createDiscountDTO.getDiscount());
        entity.setStaticCode(createDiscountDTO.getStaticCode());
        entity.setVisibleOnEyca(createDiscountDTO.getVisibleOnEyca());
        entity.setLandingPageUrl(createDiscountDTO.getLandingPageUrl());
        entity.setLandingPageReferrer(createDiscountDTO.getLandingPageReferrer());
        entity.setCondition(createDiscountDTO.getCondition());
        entity.setProducts(toEntityDiscountProduct.apply(createDiscountDTO.getProductCategories(), entity));
        entity.setState(DiscountStateEnum.DRAFT); // default state
        entity.setLastBucketCodeLoadUid(createDiscountDTO.getLastBucketCodeLoadUid());
        entity.setLastBucketCodeLoadFileName(createDiscountDTO.getLastBucketCodeLoadFileName());
        return entity;
    };

}