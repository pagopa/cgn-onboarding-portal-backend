package it.gov.pagopa.cgn.portal.converter.backoffice.approved;

import it.gov.pagopa.cgn.portal.converter.AbstractConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeDocumentConverter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreementDetail;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreementDiscount;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class BackofficeApprovedAgreementDetailConverter
        extends AbstractConverter<AgreementEntity, ApprovedAgreementDetail> {

    private BackofficeApprovedDiscountConverter discountConverter;
    private BackofficeDocumentConverter documentConverter;
    private BackofficeApprovedAgreementProfileConverter profileConverter;
    protected Function<AgreementEntity, ApprovedAgreementDetail> toDto = entity -> {
        ApprovedAgreementDetail dto = new ApprovedAgreementDetail();
        dto.setAgreementId(entity.getId());
        dto.setDiscounts((List<ApprovedAgreementDiscount>) discountConverter.toDtoCollection(entity.getDiscountList()));
        dto.setDocuments((List<Document>) documentConverter.toDtoCollection(entity.getDocumentList()));
        dto.setProfile(profileConverter.toDto(entity.getProfile()));
        return dto;
    };

    @Autowired
    public BackofficeApprovedAgreementDetailConverter(BackofficeApprovedDiscountConverter discountConverter,
                                                      BackofficeDocumentConverter documentConverter,
                                                      BackofficeApprovedAgreementProfileConverter profileConverter) {

        this.discountConverter = discountConverter;
        this.documentConverter = documentConverter;
        this.profileConverter = profileConverter;
    }

    @Override
    protected Function<AgreementEntity, ApprovedAgreementDetail> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<ApprovedAgreementDetail, AgreementEntity> toEntityFunction() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
