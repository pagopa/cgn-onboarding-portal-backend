package it.gov.pagopa.converter;


import it.gov.pagopa.cgnonboardingportal.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.model.AgreementState;
import it.gov.pagopa.cgnonboardingportal.model.ApprovedAgreement;
import it.gov.pagopa.enums.AgreementStateEnum;
import it.gov.pagopa.exception.InvalidRequestException;
import it.gov.pagopa.model.AgreementEntity;
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
                    .orElseThrow(() -> new InvalidRequestException("Enum mapping not found for " + entityEnum));

    protected Function<AgreementState, AgreementStateEnum> toEntityEnum = agreementState ->
            enumMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(agreementState))
                    .map(Map.Entry::getKey)
                    .findFirst().orElseThrow();

    private final Function<AgreementEntity, Agreement> toDtoWithStatusFilled = entity -> {
        Agreement dto;
        if (AgreementStateEnum.APPROVED.equals(entity.getState())) {
            ApprovedAgreement approvedAgreement;
            approvedAgreement = new ApprovedAgreement();
            approvedAgreement.setStartDate(entity.getStartDate());
            approvedAgreement.setEndDate(entity.getEndDate());
            dto = approvedAgreement;
        } else {
            dto = new Agreement();
        }
        return dto;
    };

    protected Function<AgreementEntity, Agreement> toDto =
            entity -> {
                Agreement dto = toDtoWithStatusFilled.apply(entity);
                dto.setId(entity.getId());
                dto.setState(toDtoEnum.apply(entity.getState()));
                dto.setProfileLastModifiedDate(entity.getProfileModifiedDate());
                dto.setDiscountsLastModifiedDate(entity.getDiscountsModifiedDate());
                dto.setDocumentsLastModifiedDate(entity.getDocumentsModifiedDate());
                return dto;
            };

    protected Function<Agreement, AgreementEntity> toEntityWithStatusFilled = dto -> {
        AgreementEntity entity = new AgreementEntity();
        if (AgreementState.APPROVEDAGREEMENT.equals(dto.getState())) {
            ApprovedAgreement state = (ApprovedAgreement) dto;
            entity.setStartDate(state.getStartDate());
            entity.setEndDate(state.getEndDate());
        }
        return entity;
    };

    protected Function<Agreement, AgreementEntity> toEntity =
            dto -> {
                AgreementEntity entity = toEntityWithStatusFilled.apply(dto);
                entity.setId(dto.getId());
                entity.setState(toEntityEnum.apply(dto.getState()));
                entity.setProfileModifiedDate(dto.getProfileLastModifiedDate());
                entity.setDiscountsModifiedDate(dto.getDiscountsLastModifiedDate());
                entity.setDocumentsModifiedDate(dto.getDocumentsLastModifiedDate());
                return entity;
            };

}