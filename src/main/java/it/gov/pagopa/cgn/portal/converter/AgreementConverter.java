package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgnonboardingportal.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.model.AgreementState;
import it.gov.pagopa.cgnonboardingportal.model.ApprovedAgreement;
import it.gov.pagopa.cgnonboardingportal.model.RejectedAgreement;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class AgreementConverter extends AbstractConverter<AgreementEntity, Agreement> {


    private static final Map<AgreementStateEnum, AgreementState> enumMap = new EnumMap<>(AgreementStateEnum.class);
    static {
        enumMap.put(AgreementStateEnum.DRAFT, AgreementState.DRAFTAGREEMENT);
        enumMap.put(AgreementStateEnum.PENDING, AgreementState.PENDINGAGREEMENT);
        enumMap.put(AgreementStateEnum.APPROVED, AgreementState.APPROVEDAGREEMENT);
        enumMap.put(AgreementStateEnum.REJECTED, AgreementState.REJECTEDAGREEMENT);
    }

    @Override
    protected Function<AgreementEntity, Agreement> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Agreement, AgreementEntity> toEntityFunction() {
        return toEntity;
    }

    protected Function<AgreementStateEnum, AgreementState> toDtoEnum = entityEnum ->
            Optional.ofNullable(enumMap.get(entityEnum))
                    .orElseThrow(() -> getInvalidEnumMapping(entityEnum.getCode()));

    protected Function<AgreementState, AgreementStateEnum> toEntityEnum = agreementState ->
            enumMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(agreementState))
                    .map(Map.Entry::getKey)
                    .findFirst().orElseThrow();

    private final Function<AgreementEntity, Agreement> toDtoWithStatusFilled = entity -> {
        Agreement dto;
        switch (entity.getState()) {
            case APPROVED:
                ApprovedAgreement approvedAgreement;
                approvedAgreement = new ApprovedAgreement();
                approvedAgreement.setStartDate(entity.getStartDate());
                approvedAgreement.setEndDate(entity.getEndDate());
                approvedAgreement.setFirstDiscountPublishingDate(entity.getFirstDiscountPublishingDate());
                dto = approvedAgreement;
                break;
            case REJECTED:
                RejectedAgreement rejectedAgreement = new RejectedAgreement();
                rejectedAgreement.setReasonMessage(entity.getRejectReasonMessage());
                dto = rejectedAgreement;
                break;
            default:
                dto = new Agreement();
        }
        return dto;
    };

    protected Function<AgreementEntity, Agreement> toDto =
            entity -> {
                Agreement dto = toDtoWithStatusFilled.apply(entity);
                dto.setId(entity.getId());
                dto.setState(toDtoEnum.apply(entity.getState()));
                dto.setImageUrl(entity.getImageUrl());
                return dto;
            };

    protected Function<Agreement, AgreementEntity> toEntityWithStatusFilled = dto -> {
        AgreementEntity entity = new AgreementEntity();
        if (AgreementState.APPROVEDAGREEMENT.equals(dto.getState())) {
            ApprovedAgreement state = (ApprovedAgreement) dto;
            entity.setStartDate(state.getStartDate());
            entity.setEndDate(state.getEndDate());
            entity.setFirstDiscountPublishingDate(state.getFirstDiscountPublishingDate());
        }
        if (AgreementState.REJECTEDAGREEMENT.equals(dto.getState())) {
            RejectedAgreement rejectedAgreement = (RejectedAgreement) dto;
            entity.setRejectReasonMessage(rejectedAgreement.getReasonMessage());
        }
        return entity;
    };

    protected Function<Agreement, AgreementEntity> toEntity =
            dto -> {
                AgreementEntity entity = toEntityWithStatusFilled.apply(dto);
                entity.setId(dto.getId());
                entity.setState(toEntityEnum.apply(dto.getState()));
                entity.setImageUrl(dto.getImageUrl());
                return entity;
            };

}