package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.BackofficeAgreementFacade;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgnonboardingportal.backoffice.api.ApprovedAgreementsApi;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreementDetail;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class BackofficeApprovedAgreementController implements ApprovedAgreementsApi {

    private final BackofficeAgreementFacade backofficeAgreementFacade;

    @Override
    public ResponseEntity<ApprovedAgreementDetail> getApprovedAgreement(String agreementId) {
        return backofficeAgreementFacade.getApprovedAgreementDetail(agreementId);
    }

    @Override
    public ResponseEntity<ApprovedAgreements> getApprovedAgreements(
            String profileFullName, LocalDate requestDateFrom, LocalDate requestDateTo,Integer pageSize, Integer page) {

        BackofficeFilter filter = BackofficeFilter.getFilter(
                profileFullName, requestDateFrom, requestDateTo, pageSize, page);
        return backofficeAgreementFacade.getApprovedAgreements(filter);
    }

    @Autowired
    public BackofficeApprovedAgreementController(BackofficeAgreementFacade backofficeAgreementFacade) {
        this.backofficeAgreementFacade = backofficeAgreementFacade;
    }
}
