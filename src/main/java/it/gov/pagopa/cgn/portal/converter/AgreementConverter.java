package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgnonboardingportal.model.*;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class AgreementConverter
        extends AbstractConverter<AgreementEntity, Agreement> {


    private static final Map<EntityTypeEnum, EntityType> entityTypeEnumMap = new EnumMap<>(EntityTypeEnum.class);
    private static final Map<AgreementStateEnum, AgreementState> enumMap = new EnumMap<>(AgreementStateEnum.class);

    static {
        enumMap.put(AgreementStateEnum.DRAFT, AgreementState.DRAFT_AGREEMENT);
        enumMap.put(AgreementStateEnum.PENDING, AgreementState.PENDING_AGREEMENT);
        enumMap.put(AgreementStateEnum.APPROVED, AgreementState.APPROVED_AGREEMENT);
        enumMap.put(AgreementStateEnum.REJECTED, AgreementState.REJECTED_AGREEMENT);

        entityTypeEnumMap.put(EntityTypeEnum.PRIVATE, EntityType.PRIVATE);
        entityTypeEnumMap.put(EntityTypeEnum.PUBLIC_ADMINISTRATION, EntityType.PUBLIC_ADMINISTRATION);
    }

    private final Function<AgreementEntity, Agreement> toDtoWithStatusFilled = entity -> {
        Agreement dto;
        switch (entity.getState()) {
            case DRAFT:
                dto = new DraftAgreement();
                break;
            case PENDING:
                dto = new PendingAgreement();
                break;
            case APPROVED:
                ApprovedAgreement approvedAgreement;
                approvedAgreement = new ApprovedAgreement();
                approvedAgreement.setStartDate(entity.getStartDate());
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
    protected Function<AgreementStateEnum, AgreementState> toDtoEnum = entityEnum -> Optional.ofNullable(enumMap.get(
            entityEnum)).orElseThrow(() -> getInvalidEnumMapping(entityEnum.getCode()));
    protected Function<AgreementState, AgreementStateEnum> toEntityEnum = agreementState -> enumMap.entrySet()
                                                                                                   .stream()
                                                                                                   .filter(entry -> entry.getValue()
                                                                                                                         .equals(agreementState))
                                                                                                   .map(Map.Entry::getKey)
                                                                                                   .findFirst()
                                                                                                   .orElseThrow();
    protected Function<AgreementEntity, Agreement> toDto = entity -> {
        Agreement dto = toDtoWithStatusFilled.apply(entity);
        dto.setId(entity.getId());
        dto.setState(toDtoEnum.apply(entity.getState()));
        dto.setImageUrl(entity.getImageUrl());
        dto.setEntityType(toEntityType(entity.getEntityType()));
        return dto;
    };
    protected Function<Agreement, AgreementEntity> toEntityWithStatusFilled = dto -> {
        AgreementEntity entity = new AgreementEntity();
        if (AgreementState.APPROVED_AGREEMENT.equals(dto.getState())) {
            ApprovedAgreement state = (ApprovedAgreement) dto;
            entity.setStartDate(state.getStartDate());
            entity.setFirstDiscountPublishingDate(state.getFirstDiscountPublishingDate());
        }
        if (AgreementState.REJECTED_AGREEMENT.equals(dto.getState())) {
            RejectedAgreement rejectedAgreement = (RejectedAgreement) dto;
            entity.setRejectReasonMessage(rejectedAgreement.getReasonMessage());
        }
        return entity;
    };
    protected Function<Agreement, AgreementEntity> toEntity = dto -> {
        AgreementEntity entity = toEntityWithStatusFilled.apply(dto);
        entity.setId(dto.getId());
        entity.setState(toEntityEnum.apply(dto.getState()));
        entity.setImageUrl(dto.getImageUrl());
        entity.setEntityType(toEntityTypeEnum(dto.getEntityType()));
        return entity;
    };

    @Override
    protected Function<AgreementEntity, Agreement> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<Agreement, AgreementEntity> toEntityFunction() {
        return toEntity;
    }

    public EntityType toEntityType(EntityTypeEnum etEnum) {
        return Optional.ofNullable(entityTypeEnumMap.get(etEnum))
                       .orElseThrow(() -> getInvalidEnumMapping(etEnum.getCode()));

    }

    public EntityTypeEnum toEntityTypeEnum(EntityType entityType) {
        return entityTypeEnumMap.entrySet()
                                .stream()
                                .filter(entry -> entry.getValue().equals(entityType))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElseThrow();
    }
}