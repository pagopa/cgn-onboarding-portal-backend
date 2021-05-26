package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.BackofficeAgreementFacade;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.backoffice.api.AgreementRequestsApi;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Agreements;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Document;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.RefuseAgreement;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.SuspendDiscount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class BackofficeAgreementController implements AgreementRequestsApi {

    private final BackofficeAgreementFacade agreementFacade;

    @Override
    public ResponseEntity<Agreements> getAgreements(
            String states,  String assignee, String profileFullName, LocalDate requestDateFrom,  LocalDate requestDateTo,
            Integer pageSize, Integer page, String sortColumn, String sortDirection) {

        var filter = BackofficeFilter.getRequestFilter(states, profileFullName, assignee, requestDateFrom,
                requestDateTo, pageSize, page, sortColumn, sortDirection);
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

    @Override
    public ResponseEntity<Void> deleteDocument(String agreementId, String documentType) {
        return agreementFacade.deleteDocument(agreementId, documentType);
    }

    @Override
    public ResponseEntity<List<Document>> getDocuments(String agreementId) {
        return agreementFacade.getDocuments(agreementId);
    }

    @Override
    public ResponseEntity<Document> uploadDocument(String agreementId, String documentType, MultipartFile document) {
        CGNUtils.checkIfPdfFile(document.getOriginalFilename());
        return agreementFacade.uploadDocument(agreementId, documentType, document);
    }

    @Override
    public ResponseEntity<Void> suspendDiscount(String agreementId, String discountId, SuspendDiscount suspension) {
        return agreementFacade.suspendDiscount(agreementId, discountId, suspension);
    }

    @Autowired
    public BackofficeAgreementController(BackofficeAgreementFacade agreementFacade) {
        this.agreementFacade = agreementFacade;
    }

}
