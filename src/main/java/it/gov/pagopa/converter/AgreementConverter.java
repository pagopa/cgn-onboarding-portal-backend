package it.gov.pagopa.converter;


import it.gov.pagopa.cgnonboardingportal.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.model.AgreementState;
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


    protected Function<AgreementEntity, Agreement> toDto =
            entity -> {
                Agreement dto = new Agreement();
                dto.setId(entity.getId());
                dto.setState(toDtoEnum.apply(entity.getState()));
                dto.setProfileLastModifiedDate(entity.getProfileModifiedDate());
                dto.setDiscountsLastModifiedDate(entity.getDiscountsModifiedDate());
                dto.setDocumentsLastModifiedDate(entity.getDocumentsModifiedDate());
                return dto;
            };

    protected Function<Agreement, AgreementEntity> toEntity =
            dto -> {
                AgreementEntity entity = new AgreementEntity();
                entity.setId(dto.getId());
                entity.setState(toEntityEnum.apply(dto.getState()));
                entity.setProfileModifiedDate(dto.getProfileLastModifiedDate());
                entity.setDiscountsModifiedDate(dto.getDiscountsLastModifiedDate());
                entity.setDocumentsModifiedDate(dto.getDocumentsLastModifiedDate());
                return entity;
            };

}