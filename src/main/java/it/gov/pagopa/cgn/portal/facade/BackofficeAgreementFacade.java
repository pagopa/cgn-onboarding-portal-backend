package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.service.BackofficeAgreementService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Agreements;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.RefuseAgreement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;


@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Component
public class BackofficeAgreementFacade {

    private final BackofficeAgreementService service;

    private final BackofficeAgreementConverter agreementConverter;

    @Transactional(Transactional.TxType.REQUIRED)   // for converter
    public ResponseEntity<Agreements> getAgreements(BackofficeFilter filter) {
        Page<AgreementEntity> agreements = service.getAgreements(filter);
        return ResponseEntity.ok(agreementConverter.getAgreementFromPage(agreements));
    }

    public ResponseEntity<Void> assignAgreement(String agreementId) {
        service.assignAgreement(agreementId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> unassignAgreement(String agreementId) {
        service.unassignAgreement(agreementId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> approveAgreement(String agreementId) {
        service.approveAgreement(agreementId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> rejectAgreement(String agreementId, RefuseAgreement refusal) {
        service.rejectAgreement(agreementId, refusal.getReasonMessage());
        return ResponseEntity.noContent().build();
    }

    @Autowired
    public BackofficeAgreementFacade(BackofficeAgreementService service, BackofficeAgreementConverter agreementConverter) {
        this.service = service;
        this.agreementConverter = agreementConverter;
    }
}
