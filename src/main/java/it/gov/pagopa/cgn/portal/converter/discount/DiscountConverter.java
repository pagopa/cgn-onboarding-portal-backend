package it.gov.pagopa.cgn.portal.converter.discount;

import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgnonboardingportal.model.Discount;
import it.gov.pagopa.cgnonboardingportal.model.Discounts;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DiscountConverter
        extends CommonDiscountConverter<DiscountEntity, Discount> {

    protected Function<DiscountEntity, Discount> toDto = entity -> {
        Discount dto = new Discount();
        dto.setId(String.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setNameDe(entity.getNameDe());
        dto.setDescription(entity.getDescription());
        dto.setDescriptionEn(entity.getDescriptionEn());
        dto.setDescriptionDe(entity.getDescriptionDe());
        dto.setDiscount(entity.getDiscountValue());
        dto.setAgreementId(entity.getAgreement().getId());
        dto.setCondition(entity.getCondition());
        dto.setConditionEn(entity.getConditionEn());
        dto.setConditionDe(entity.getConditionDe());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStaticCode(entity.getStaticCode());
        dto.setVisibleOnEyca(entity.getVisibleOnEyca());
        dto.setLandingPageUrl(entity.getLandingPageUrl());
        dto.setLandingPageReferrer(entity.getLandingPageReferrer());
        dto.setState(toDtoEnum.apply(entity.getState(), entity.getEndDate()));
        dto.setProductCategories(toProductDtoListEnum.apply(entity.getProducts()));
        dto.setCreationDate(LocalDate.from(entity.getInsertTime()));
        dto.setSuspendedReasonMessage(entity.getSuspendedReasonMessage());
        dto.setDiscountUrl(entity.getDiscountUrl());
        dto.setTestFailureReason(entity.getTestFailureReason());
        if (entity.getLastBucketCodeLoad()!=null) {
            dto.setLastBucketCodeLoadUid(entity.getLastBucketCodeLoad().getUid());
            dto.setLastBucketCodeLoadFileName(entity.getLastBucketCodeLoad().getFileName());
            dto.setLastBucketCodeLoadStatus(toBucketCodeLoadStatusDtoEnum.apply(entity.getLastBucketCodeLoad()
                                                                                      .getStatus()));
        }
        return dto;
    };
    protected Function<List<DiscountEntity>, Discounts> toDiscountsDto = discountEntities -> {
        List<Discount> discountList = CollectionUtils.isEmpty(discountEntities) ?
                                      Collections.emptyList():
                                      discountEntities.stream().map(toDtoFunction()).collect(Collectors.toList());
        Discounts discounts = new Discounts();
        discounts.setItems(discountList);
        return discounts;
    };
    protected Function<Discount, DiscountEntity> toEntity = dto -> {
        throw new UnsupportedOperationException("Not implemented yet");
    };

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

}
