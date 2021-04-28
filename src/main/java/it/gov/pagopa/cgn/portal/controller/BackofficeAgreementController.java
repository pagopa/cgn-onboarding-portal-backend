package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.BackofficeAgreementFacade;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgnonboardingportal.backoffice.api.BackofficeApi;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Agreements;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.RefuseAgreement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class BackofficeAgreementController implements BackofficeApi {

    private final BackofficeAgreementFacade agreementFacade;

    @Override
    public ResponseEntity<Agreements> getAgreements(String states, String assignee, String profileFullName,
                                                    LocalDate requestDateFrom, LocalDate requestDateTo,
                                                    Integer pageSize, Integer page) {
        BackofficeFilter filter;
        filter = getFilter(states, profileFullName, assignee, requestDateFrom, requestDateTo, pageSize, page);
        return agreementFacade.getAgreements(filter);
    }

    @Override
    public ResponseEntity<Void> assignAgreement(String agreementId) {
        return agreementFacade.assignAgreement(agreementId);
    }

    @Override
    public ResponseEntity<Void> unassignAgreement(String agreementId) {
        return agreementFacade.unassignAgreement(agreementId);
    }

    @Override
    public ResponseEntity<Void> approveAgreement(String agreementId) {
        return agreementFacade.approveAgreement(agreementId);
    }

    @Override
    public ResponseEntity<Void> rejectAgreement(String agreementId, RefuseAgreement refusal) {
        return agreementFacade.rejectAgreement(agreementId, refusal);
    }

    @Autowired
    public BackofficeAgreementController(BackofficeAgreementFacade agreementFacade) {
        this.agreementFacade = agreementFacade;
    }

    private BackofficeFilter getFilter(String state, String profileFullName, String assignee, LocalDate startDateFrom,
                                       LocalDate startDateTo, Integer pageSize, Integer page) {
        return BackofficeFilter.builder()
                .agreementState(state)
                .profileFullName(profileFullName)
                .assignee(assignee)
                .requestDateFrom(startDateFrom)
                .requestDateTo(startDateTo)
                .page(page)
                .pageSize(pageSize).build();
    }
}
