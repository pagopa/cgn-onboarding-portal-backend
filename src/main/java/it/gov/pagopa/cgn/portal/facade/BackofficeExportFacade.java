package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.service.ExportService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class BackofficeExportFacade {

    private final ExportService exportService;

    public BackofficeExportFacade(ExportService exportService) {
        this.exportService = exportService;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Resource> exportAgreements() {
        return exportService.exportAgreements();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Resource> exportEycaDiscounts() {
        return exportService.exportEycaDiscounts();
    }
}
