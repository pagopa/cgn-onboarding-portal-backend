package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgnonboardingportal.backoffice.api.ApprovedAgreementsApi;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreementDetail;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class BackofficeApprovedAgreementController implements ApprovedAgreementsApi {


    @Override
    public ResponseEntity<ApprovedAgreementDetail> getApprovedAgreement(String agreementId) {
       throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ResponseEntity<ApprovedAgreements> getApprovedAgreements(
            String profileFullName, LocalDate requestDateFrom, LocalDate requestDateTo,Integer pageSize, Integer page) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
