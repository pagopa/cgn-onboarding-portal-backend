package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.DocumentConverter;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.service.DocumentService;
import it.gov.pagopa.cgnonboardingportal.model.Document;
import it.gov.pagopa.cgnonboardingportal.model.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class DocumentFacade {

    private final DocumentService documentService;
    private final DocumentConverter documentConverter;


    public ResponseEntity<Documents> getDocuments(String agreementId) {
        List<DocumentEntity> documentList = documentService.getDocuments(agreementId);
        Documents documents = documentConverter.getDocumentsDtoFromDocumentEntityList(documentList);
        return ResponseEntity.ok(documents);
    }

    public ResponseEntity<Document> uploadDocument(String agreementId, String documentType, InputStream content, long size) throws IOException {
        DocumentEntity documentEntity = documentService.storeDocument(agreementId, DocumentTypeEnum.valueOf(documentType.toUpperCase()), content, size);
        return ResponseEntity.ok(documentConverter.toDto(documentEntity));
    }

    public void deleteDocument(String agreementId, String documentType) {
        documentService.deleteDocument(agreementId, DocumentTypeEnum.valueOf(documentType.toUpperCase()));
    }

    @Autowired
    public DocumentFacade(DocumentService documentService, DocumentConverter documentConverter) {
        this.documentService = documentService;
        this.documentConverter = documentConverter;
    }
}
