package it.gov.pagopa.cgn.portal.converter.backoffice.approved;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreement;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreements;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

@Component
public class BackofficeApprovedAgreementConverter extends AbstractConverter<ApprovedAgreementEntity, ApprovedAgreement> {

    @Override
    protected Function<ApprovedAgreementEntity, ApprovedAgreement> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<ApprovedAgreement, ApprovedAgreementEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected Function<ApprovedAgreementEntity, ApprovedAgreement> toDto =
            entity -> {
                ApprovedAgreement dto = new ApprovedAgreement();
                dto.setAgreementId(entity.getId());
                dto.setAgreementLastUpdateDate(entity.getInformationLastUpdateDate());
                dto.setFullName(entity.getFullName());
                dto.setAgreementStartDate(entity.getStartDate());
                dto.setPublishedDiscounts(entity.getPublishedDiscounts());
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
