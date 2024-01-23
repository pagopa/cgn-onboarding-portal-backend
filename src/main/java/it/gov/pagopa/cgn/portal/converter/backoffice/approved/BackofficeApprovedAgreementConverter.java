package it.gov.pagopa.cgn.portal.converter.backoffice.approved;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreement;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreements;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class BackofficeApprovedAgreementConverter
        extends AbstractConverter<ApprovedAgreementEntity, ApprovedAgreement> {
    private static final Map<EntityTypeEnum, EntityType> backofficeEntityTypeEnumMap = new EnumMap<>(EntityTypeEnum.class);

    static {
        backofficeEntityTypeEnumMap.put(
                EntityTypeEnum.PRIVATE, EntityType.PRIVATE);
        backofficeEntityTypeEnumMap.put(
                EntityTypeEnum.PUBLIC_ADMINISTRATION, EntityType.PUBLICADMINISTRATION);
    }

    public static EntityType getEntityTypeFromEntityTypeEnum(EntityTypeEnum etEnum) {
        return Optional.ofNullable(backofficeEntityTypeEnumMap.get(etEnum))
                .orElseThrow(() -> getInvalidEnumMapping(etEnum.getCode()));

    }

    @Override
    protected Function<ApprovedAgreementEntity, ApprovedAgreement> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<ApprovedAgreement, ApprovedAgreementEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected Function<ApprovedAgreementEntity, ApprovedAgreement> toDto = entity -> {
        ApprovedAgreement dto = new ApprovedAgreement();
        dto.setAgreementId(entity.getId());
        dto.setAgreementLastUpdateDate(entity.getInformationLastUpdateDate());
        dto.setFullName(entity.getFullName());
        dto.setAgreementStartDate(entity.getStartDate());
        dto.setPublishedDiscounts(entity.getPublishedDiscounts());
        dto.setTestPending(entity.getTestPending());
        dto.setEntityType(getEntityTypeFromEntityTypeEnum(entity.getEntityType()));
        return dto;
    };

    public ApprovedAgreements getApprovedAgreementsFromPage(Page<ApprovedAgreementEntity> agreementEntityPage) {
        Collection<ApprovedAgreement> dtoCollection = toDtoCollection(agreementEntityPage.getContent());
        ApprovedAgreements approvedAgreements = new ApprovedAgreements();
        approvedAgreements.setItems(new ArrayList<>(dtoCollection));
        approvedAgreements.setTotal((int) agreementEntityPage.getTotalElements());
        return approvedAgreements;
    }
}
