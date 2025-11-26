package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.DocumentConverter;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.service.DocumentService;
import it.gov.pagopa.cgnonboardingportal.model.BucketLoad;
import it.gov.pagopa.cgnonboardingportal.model.Document;
import it.gov.pagopa.cgnonboardingportal.model.Documents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class DocumentFacade {

    private final DocumentService documentService;
    private final DocumentConverter documentConverter;
    private final AzureStorage azureStorage;

    @Autowired
    public DocumentFacade(DocumentService documentService,
                          DocumentConverter documentConverter,
                          AzureStorage azureStorage) {
        this.documentService = documentService;
        this.documentConverter = documentConverter;
        this.azureStorage = azureStorage;
    }

    public ResponseEntity<Resource> getDocumentTemplate(String agreementId, String documentType) {
        byte[] document = documentService.renderDocument(agreementId,
                                                         DocumentTypeEnum.fromValue(documentType.toUpperCase()))
                                         .toByteArray();

        return ResponseEntity.ok()
                             .contentLength(document.length)
                             .contentType(MediaType.APPLICATION_PDF)
                             .cacheControl(CacheControl.noCache().mustRevalidate())
                             .body(new ByteArrayResource(document));
    }

    public ResponseEntity<Documents> getDocuments(String agreementId) {
        List<DocumentEntity> documentList = documentService.getPrioritizedDocuments(agreementId);
        azureStorage.setSecureDocumentUrl(documentList);
        Documents documents = documentConverter.getDocumentsDtoFromDocumentEntityList(documentList);
        return ResponseEntity.ok(documents);
    }

    public ResponseEntity<Document> uploadDocument(String agreementId, String documentType, MultipartFile document) {
        DocumentEntity documentEntity;
        try {
            documentEntity = documentService.storeDocument(agreementId,
                                                           DocumentTypeEnum.fromValue(documentType),
                                                           document.getInputStream(),
                                                           document.getSize());
        } catch (IOException e) {
            throw new CGNException(e);
        }
        azureStorage.setSecureDocumentUrl(documentEntity);
        return ResponseEntity.ok(documentConverter.toDto(documentEntity));
    }

    public ResponseEntity<BucketLoad> uploadBucket(String agreementId, MultipartFile document) {
        String bucketLoadUID;
        try {
            bucketLoadUID = documentService.storeBucket(agreementId, document.getInputStream());
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
        BucketLoad bucketLoad = new BucketLoad();
        bucketLoad.setUid(bucketLoadUID);
        return ResponseEntity.ok(bucketLoad);
    }

    public long deleteDocument(String agreementId, String documentType) {
        return documentService.deleteDocument(agreementId, DocumentTypeEnum.fromValue(documentType));
    }

}
