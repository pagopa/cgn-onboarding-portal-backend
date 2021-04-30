package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeDocumentConverter;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.service.BackofficeAgreementService;
import it.gov.pagopa.cgn.portal.service.DocumentService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Agreements;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Document;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.RefuseAgreement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Component
public class BackofficeAgreementFacade {

    private final BackofficeAgreementService service;

    private final BackofficeAgreementConverter agreementConverter;

    private final BackofficeDocumentConverter documentConverter;

    private final DocumentService documentService;

    private final AzureStorage azureStorage;


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

    public ResponseEntity<Void> deleteDocument(String agreementId, String documentType) {
        DocumentTypeEnum documentTypeEnum = documentConverter.getBackofficeDocumentTypeEnum(documentType);
        long deleteDocument = documentService.deleteDocument(agreementId, documentTypeEnum);
        if (deleteDocument != 1) {
            throw new InvalidRequestException(
                    String.format("Document with agreement %s and document type %s not found", agreementId, documentType));
        }
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<List<Document>> getDocuments(String agreementId) {
        List<DocumentEntity> backofficeDocuments = documentService.getAllDocuments(agreementId,
                documentEntity -> documentEntity.getDocumentType().isBackoffice());
        return ResponseEntity.ok(new ArrayList<>(documentConverter.toDtoCollection(backofficeDocuments)));
    }


    public ResponseEntity<Document> uploadDocument(String agreementId, String documentType, MultipartFile document) {
        DocumentEntity documentEntity;
        try {
            documentEntity = documentService.storeDocument(agreementId,
                    documentConverter.getBackofficeDocumentTypeEnum(documentType), document.getInputStream(),
                    document.getSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        azureStorage.setSecureDocumentUrl(documentEntity);
        return ResponseEntity.ok(documentConverter.toDto(documentEntity));
    }



    @Autowired
    public BackofficeAgreementFacade(BackofficeAgreementService service, BackofficeAgreementConverter agreementConverter,
                                     BackofficeDocumentConverter documentConverter, DocumentService documentService,
                                     AzureStorage azureStorage) {
        this.service = service;
        this.agreementConverter = agreementConverter;
        this.documentConverter = documentConverter;
        this.documentService = documentService;
        this.azureStorage = azureStorage;
    }
}
