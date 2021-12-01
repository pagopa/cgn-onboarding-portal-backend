package it.gov.pagopa.cgn.portal.converter.discount;

import it.gov.pagopa.cgnonboardingportal.model.CreateDiscount;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.model.Discount;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
        CreateDiscount dto = new CreateDiscount();
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setDiscount(entity.getDiscountValue());
        dto.setCondition(entity.getCondition());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStaticCode(entity.getStaticCode());
        dto.setVisibleOnEyca(entity.getVisibleOnEyca());
        dto.setLandingPageUrl(entity.getLandingPageUrl());
        dto.setLandingPageReferrer(entity.getLandingPageReferrer());
        dto.setProductCategories(toProductDtoListEnum.apply(entity.getProducts()));
        dto.setLastBucketCodeFileUid(entity.getLastBucketCodeFileUid());
        return dto;
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
        entity.setLastBucketCodeFileUid(createDiscountDTO.getLastBucketCodeFileUid());
        return entity;
    };

}