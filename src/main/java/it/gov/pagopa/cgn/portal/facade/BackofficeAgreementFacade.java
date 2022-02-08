package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeDocumentConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.approved.BackofficeApprovedAgreementConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.approved.BackofficeApprovedAgreementDetailConverter;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.service.*;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BackofficeAgreementFacade {

    private final BackofficeAgreementService backofficeAgreementService;

    private final ApprovedAgreementService approvedAgreementService;

    private final AgreementService agreementService;

    private final BackofficeAgreementConverter agreementConverter;

    private final BackofficeDocumentConverter documentConverter;

    private final BackofficeApprovedAgreementDetailConverter agreementDetailConverter;

    private final BackofficeApprovedAgreementConverter approvedAgreementConverter;

    private final DocumentService documentService;

    private final DiscountService discountService;

    private final AzureStorage azureStorage;


    @Transactional(readOnly = true)  // for converter
    public ResponseEntity<Agreements> getAgreements(BackofficeFilter filter) {
        Page<AgreementEntity> agreements = backofficeAgreementService.getAgreements(filter);
        return ResponseEntity.ok(agreementConverter.getAgreementFromPage(agreements));
    }

    public ResponseEntity<ApprovedAgreements> getApprovedAgreements(BackofficeFilter filter) {
        Page<ApprovedAgreementEntity> agreements = approvedAgreementService.getApprovedAgreements(filter);
        return ResponseEntity.ok(approvedAgreementConverter.getApprovedAgreementsFromPage(agreements));
    }

    @Transactional(readOnly = true)  // for converter
    public ResponseEntity<ApprovedAgreementDetail> getApprovedAgreementDetail(String agreementId) {
        AgreementEntity agreement = agreementService.getApprovedAgreement(agreementId);
        return ResponseEntity.ok(agreementDetailConverter.toDto(agreement));
    }

    public ResponseEntity<Void> assignAgreement(String agreementId) {
        backofficeAgreementService.assignAgreement(agreementId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> unassignAgreement(String agreementId) {
        backofficeAgreementService.unassignAgreement(agreementId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> approveAgreement(String agreementId) {
        backofficeAgreementService.approveAgreement(agreementId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> rejectAgreement(String agreementId, RefuseAgreement refusal) {
        backofficeAgreementService.rejectAgreement(agreementId, refusal.getReasonMessage());
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

    public ResponseEntity<Void> suspendDiscount(String agreementId, String discountId, SuspendDiscount suspension) {
        discountService.suspendDiscount(agreementId, Long.valueOf(discountId), suspension.getReasonMessage());
        return ResponseEntity.noContent().build();
    }

    @Autowired
    public BackofficeAgreementFacade(BackofficeAgreementService backofficeAgreementService, BackofficeAgreementConverter agreementConverter,
                                     BackofficeDocumentConverter documentConverter, DocumentService documentService,
                                     AgreementService agreementService, DiscountService discountService,
                                     BackofficeApprovedAgreementDetailConverter agreementDetailConverter,
                                     BackofficeApprovedAgreementConverter approvedAgreementConverter,
                                     AzureStorage azureStorage, ApprovedAgreementService approvedAgreementService) {
        this.backofficeAgreementService = backofficeAgreementService;
        this.agreementService = agreementService;
        this.discountService = discountService;
        this.agreementConverter = agreementConverter;
        this.documentConverter = documentConverter;
        this.agreementDetailConverter = agreementDetailConverter;
        this.approvedAgreementConverter = approvedAgreementConverter;
        this.documentService = documentService;
        this.azureStorage = azureStorage;
        this.approvedAgreementService = approvedAgreementService;
    }
}
