package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.BackofficeExportFacade;
import it.gov.pagopa.cgnonboardingportal.backoffice.api.ExportApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;


@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class BackofficeExportController
        implements ExportApi {

    private final BackofficeExportFacade backofficeExportFacade;

    @Autowired
    public BackofficeExportController(BackofficeExportFacade backofficeExportFacade) {
        this.backofficeExportFacade = backofficeExportFacade;
    }

    @Override
    public ResponseEntity<Resource> exportAgreements() {
        return backofficeExportFacade.exportAgreements();
    }

    @Override
    public ResponseEntity<Resource> exportEycaDiscounts() {
        return backofficeExportFacade.exportEycaDiscounts();
    }

}